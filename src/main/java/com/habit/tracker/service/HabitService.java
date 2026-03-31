package com.habit.tracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.habit.tracker.entity.HabitEntry;
import com.habit.tracker.repository.HabitEntryRepository;

public class HabitService {

    private final HabitEntryRepository habitEntryRepository;

    public HabitService(HabitEntryRepository habitEntryRepository) {
        this.habitEntryRepository = habitEntryRepository;
    }

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

    public List<HabitEntry> getWeeklyEntries(UUID habitId) {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        return habitEntryRepository
                .findByHabitIdAndDateBetween(habitId, start, end);
    }

}
