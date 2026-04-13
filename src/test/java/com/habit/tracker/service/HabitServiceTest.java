package com.habit.tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.response.HabitResponse;
import com.habit.tracker.entity.Habit;
import com.habit.tracker.entity.HabitEntry;
import com.habit.tracker.entity.User;
import com.habit.tracker.exception.BadRequestException;
import com.habit.tracker.repository.HabitEntryRepository;
import com.habit.tracker.repository.HabitRepository;
import com.habit.tracker.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitEntryRepository habitEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HabitService habitService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("Test User", "test@example.com", "hashed");
        var auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createHabit_success_returnsResponse() {
        CreateHabitRequest request = new CreateHabitRequest();
        request.setName("Morning Run");
        request.setDescription("Run 5k every morning");

        when(habitRepository.save(any(Habit.class))).thenAnswer(i -> i.getArgument(0));

        HabitResponse response = habitService.createHabit(request);

        assertThat(response.getName()).isEqualTo("Morning Run");
        assertThat(response.getStreak()).isZero();
        assertThat(response.isCompletedToday()).isFalse();
    }

    @Test
    void getHabits_returnsAllUserHabits() {
        Habit habit = new Habit();
        habit.setName("Read");
        habit.setDescription("Read 30 mins");

        Pageable pageable = PageRequest.of(0, 100);
        when(habitRepository.findByUserId(eq(currentUser.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(habit)));
        when(habitEntryRepository.findByHabitIdAndDate(any(), any())).thenReturn(Optional.empty());
        when(habitEntryRepository.findByHabitIdOrderByDateDesc(any())).thenReturn(List.of());

        List<HabitResponse> habits = habitService.getHabits(pageable);

        assertThat(habits).hasSize(1);
        assertThat(habits.get(0).getName()).isEqualTo("Read");
    }

    @Test
    void deleteHabit_notOwnedByUser_throwsBadRequest() {
        UUID habitId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User otherUser = new User("Other", "other@example.com", "hashed");
        // Set a different user ID on the habit's owner
        Habit habit = new Habit();
        habit.setUser(otherUser);

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));

        assertThatThrownBy(() -> habitService.deleteHabit(habitId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void completeHabit_sameDay_doesNotDoubleAwardXp() {
        UUID habitId = UUID.randomUUID();
        Habit habit = new Habit();
        habit.setUser(currentUser);

        HabitEntry existingEntry = new HabitEntry(LocalDate.now(), true, habit);

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));
        when(habitEntryRepository.findByHabitIdAndDate(habitId, LocalDate.now()))
                .thenReturn(Optional.of(existingEntry));

        habitService.completeHabit(habitId);

        // XP should not be awarded again — userRepository.save should not be called
        verify(userRepository, never()).save(any());
    }

    @Test
    void uncompleteHabit_completedEntry_deductsXpAndMarksIncomplete() {
        UUID habitId = UUID.randomUUID();
        Habit habit = new Habit();
        habit.setUser(currentUser);
        currentUser.setXp(50);

        HabitEntry entry = new HabitEntry(LocalDate.now(), true, habit);

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));
        when(habitEntryRepository.findByHabitIdAndDate(habitId, LocalDate.now()))
                .thenReturn(Optional.of(entry));
        when(habitEntryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        habitService.uncompleteHabit(habitId);

        assertThat(entry.isCompleted()).isFalse();
        assertThat(currentUser.getXp()).isEqualTo(40);
    }

    @Test
    void calculateStreak_consecutiveDays_returnsCorrectCount() {
        UUID habitId = UUID.randomUUID();

        HabitEntry today = new HabitEntry(LocalDate.now(), true, new Habit());
        HabitEntry yesterday = new HabitEntry(LocalDate.now().minusDays(1), true, new Habit());
        HabitEntry twoDaysAgo = new HabitEntry(LocalDate.now().minusDays(2), true, new Habit());

        when(habitEntryRepository.findByHabitIdOrderByDateDesc(habitId))
                .thenReturn(List.of(today, yesterday, twoDaysAgo));

        int streak = habitService.calculateStreak(habitId);

        assertThat(streak).isEqualTo(3);
    }
}
