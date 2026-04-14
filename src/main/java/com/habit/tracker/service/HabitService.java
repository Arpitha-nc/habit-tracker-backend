package com.habit.tracker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.request.UpdateHabitRequest;
import com.habit.tracker.dto.response.DashboardResponse;
import com.habit.tracker.dto.response.HabitResponse;
import com.habit.tracker.dto.response.HeatmapResponse;
import com.habit.tracker.dto.response.WeeklyProgressResponse;
import com.habit.tracker.entity.Habit;
import com.habit.tracker.entity.HabitEntry;
import com.habit.tracker.entity.User;
import com.habit.tracker.exception.BadRequestException;
import com.habit.tracker.exception.ResourceNotFoundException;
import com.habit.tracker.mapper.HabitMapper;
import com.habit.tracker.repository.HabitEntryRepository;
import com.habit.tracker.repository.HabitRepository;
import com.habit.tracker.repository.UserRepository;
import com.habit.tracker.util.CurrentUserUtil;

@Service
public class HabitService {

    private static final int XP_PER_COMPLETION = 10;

    private final HabitRepository habitRepository;
    private final HabitEntryRepository habitEntryRepository;
    private final UserRepository userRepository;

    public HabitService(HabitRepository habitRepository,
            HabitEntryRepository habitEntryRepository,
            UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.habitEntryRepository = habitEntryRepository;
        this.userRepository = userRepository;
    }

    // CREATE HABIT
    public HabitResponse createHabit(CreateHabitRequest request) {
        User user = CurrentUserUtil.getCurrentUser();

        Habit habit = HabitMapper.toEntity(request);
        habit.setUser(user);
        habitRepository.save(habit);

        return HabitMapper.toResponse(habit, 0, false);
    }

    // GET ALL USER HABITS (with optional pagination)
    public List<HabitResponse> getHabits(Pageable pageable) {
        User user = CurrentUserUtil.getCurrentUser();
        List<Habit> habits = habitRepository.findByUserId(user.getId(), pageable).getContent();
        LocalDate today = LocalDate.now();

        return habits.stream()
                .map(habit -> {
                    int streak = calculateStreak(habit.getId());
                    boolean completedToday = habitEntryRepository
                            .findByHabitIdAndDate(habit.getId(), today)
                            .map(HabitEntry::isCompleted)
                            .orElse(false);
                    return HabitMapper.toResponse(habit, streak, completedToday);
                })
                .collect(Collectors.toList());
    }

    // GET SINGLE HABIT
    public HabitResponse getHabit(UUID habitId) {
        User user = CurrentUserUtil.getCurrentUser();
        Habit habit = findHabitOwnedByUser(habitId, user.getId());
        boolean completedToday = habitEntryRepository
                .findByHabitIdAndDate(habitId, LocalDate.now())
                .map(HabitEntry::isCompleted)
                .orElse(false);

        return HabitMapper.toResponse(habit, calculateStreak(habitId), completedToday);
    }

    // UPDATE HABIT
    public HabitResponse updateHabit(UUID habitId, UpdateHabitRequest request) {
        User user = CurrentUserUtil.getCurrentUser();
        Habit habit = findHabitOwnedByUser(habitId, user.getId());

        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habitRepository.save(habit);

        boolean completedToday = habitEntryRepository
                .findByHabitIdAndDate(habitId, LocalDate.now())
                .map(HabitEntry::isCompleted)
                .orElse(false);

        return HabitMapper.toResponse(habit, calculateStreak(habitId), completedToday);
    }

    // DELETE HABIT
    public void deleteHabit(UUID habitId) {
        User user = CurrentUserUtil.getCurrentUser();
        findHabitOwnedByUser(habitId, user.getId());
        habitRepository.deleteById(habitId);
    }

    // UNCOMPLETE HABIT (undo today's completion)
    public void uncompleteHabit(UUID habitId) {
        User user = CurrentUserUtil.getCurrentUser();
        findHabitOwnedByUser(habitId, user.getId());

        habitEntryRepository.findByHabitIdAndDate(habitId, LocalDate.now())
                .ifPresent(entry -> {
                    if (entry.isCompleted()) {
                        entry.setCompleted(false);
                        habitEntryRepository.save(entry);
                        user.setXp(Math.max(0, user.getXp() - XP_PER_COMPLETION));
                        userRepository.save(user);
                    }
                });
    }

    // COMPLETE HABIT
    public void completeHabit(UUID habitId) {
        User user = CurrentUserUtil.getCurrentUser();
        Habit habit = findHabitOwnedByUser(habitId, user.getId());

        LocalDate today = LocalDate.now();
        boolean[] isNewCompletion = { false };

        HabitEntry entry = habitEntryRepository
                .findByHabitIdAndDate(habitId, today)
                .orElseGet(() -> {
                    isNewCompletion[0] = true;
                    HabitEntry newEntry = new HabitEntry();
                    newEntry.setHabit(habit);
                    newEntry.setDate(today);
                    newEntry.setCompleted(true);
                    return newEntry;
                });

        if (!entry.isCompleted()) {
            isNewCompletion[0] = true;
        }

        entry.setCompleted(true);
        habitEntryRepository.save(entry);

        if (isNewCompletion[0]) {
            user.setXp(user.getXp() + XP_PER_COMPLETION);
            userRepository.save(user);
        }
    }

    // DASHBOARD STATISTICS
    public DashboardResponse getDashboard() {
        User user = CurrentUserUtil.getCurrentUser();
        UUID userId = user.getId();

        int totalHabits = (int) habitRepository.countByUserId(userId);
        int completedToday = (int) habitEntryRepository.countCompletedByUserIdAndDate(userId, LocalDate.now());

        List<Habit> habits = habitRepository.findByUserId(userId);
        int longestStreak = habits.stream()
                .mapToInt(h -> calculateStreak(h.getId()))
                .max()
                .orElse(0);

        int xp = user.getXp();
        int level = xp / 100 + 1;

        return new DashboardResponse(totalHabits, completedToday, longestStreak, xp, level);
    }

    // STREAK CALCULATION
    public int calculateStreak(UUID habitId) {
        List<HabitEntry> entries = habitEntryRepository.findByHabitIdOrderByDateDesc(habitId);

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (HabitEntry entry : entries) {
            if (!entry.isCompleted()) {
                break;
            }
            if (entry.getDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    // WEEKLY PROGRESS
    public List<WeeklyProgressResponse> getWeeklyProgress() {
        User user = CurrentUserUtil.getCurrentUser();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<HabitEntry> entries = habitEntryRepository.findByUserIdAndDateBetween(user.getId(), start, end);

        Map<LocalDate, Long> grouped = entries.stream()
                .filter(HabitEntry::isCompleted)
                .collect(Collectors.groupingBy(HabitEntry::getDate, Collectors.counting()));

        List<WeeklyProgressResponse> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            int count = grouped.getOrDefault(date, 0L).intValue();
            result.add(new WeeklyProgressResponse(date, count));
        }

        return result;
    }

    // HEATMAP (365 days)
    public List<HeatmapResponse> getHeatmap() {
        User user = CurrentUserUtil.getCurrentUser();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(364);

        List<HabitEntry> entries = habitEntryRepository.findByUserIdAndDateBetween(user.getId(), start, end);

        Map<LocalDate, Long> grouped = entries.stream()
                .filter(HabitEntry::isCompleted)
                .collect(Collectors.groupingBy(HabitEntry::getDate, Collectors.counting()));

        List<HeatmapResponse> result = new ArrayList<>();
        for (int i = 0; i <= 364; i++) {
            LocalDate date = start.plusDays(i);
            int count = grouped.getOrDefault(date, 0L).intValue();
            result.add(new HeatmapResponse(date, count));
        }

        return result;
    }

    // HELPER: verify habit belongs to user
    private Habit findHabitOwnedByUser(UUID habitId, UUID userId) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));

        if (!habit.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied: habit does not belong to the current user");
        }

        return habit;
    }

}
