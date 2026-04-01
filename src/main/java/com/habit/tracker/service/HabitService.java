package com.habit.tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.response.HabitResponse;
import com.habit.tracker.entity.Habit;
import com.habit.tracker.entity.HabitEntry;
import com.habit.tracker.entity.User;
import com.habit.tracker.mapper.HabitMapper;
import com.habit.tracker.repository.HabitEntryRepository;
import com.habit.tracker.repository.HabitRepository;
import com.habit.tracker.util.CurrentUserUtil;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitEntryRepository habitEntryRepository;

    public HabitService(HabitRepository habitRepository,
            HabitEntryRepository habitEntryRepository) {
        this.habitRepository = habitRepository;
        this.habitEntryRepository = habitEntryRepository;
    }

    // CREATE HABIT
    public HabitResponse createHabit(CreateHabitRequest request) {

        User user = CurrentUserUtil.getCurrentUser();

        Habit habit = HabitMapper.toEntity(request);
        habit.setUser(user);

        habitRepository.save(habit);

        return HabitMapper.toResponse(habit, 0);
    }

    // GET USER HABITS
    public List<HabitResponse> getHabits() {

        User user = CurrentUserUtil.getCurrentUser();

        List<Habit> habits = habitRepository.findByUserId(user.getId());

        return habits.stream()
                .map(habit -> HabitMapper.toResponse(habit, calculateStreak(habit.getId())))
                .collect(Collectors.toList());
    }

    // DELETE HABIT
    public void deleteHabit(UUID habitId) {

        habitRepository.deleteById(habitId);
    }

    // COMPLETE HABIT
    public void completeHabit(UUID habitId) {

        LocalDate today = LocalDate.now();

        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        HabitEntry entry = habitEntryRepository
                .findByHabitIdAndDate(habitId, today)
                .orElseGet(() -> {

                    HabitEntry newEntry = new HabitEntry();
                    newEntry.setHabit(habit);
                    newEntry.setDate(today);
                    newEntry.setCompleted(true);

                    return newEntry;
                });

        entry.setCompleted(true);

        habitEntryRepository.save(entry);
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
    public List<HabitEntry> getWeeklyEntries(UUID habitId) {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        return habitEntryRepository
                .findByHabitIdAndDateBetween(habitId, start, end);
    }
}