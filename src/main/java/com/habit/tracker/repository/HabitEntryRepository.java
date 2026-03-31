package com.habit.tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habit.tracker.entity.HabitEntry;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, UUID> {

    Optional<HabitEntry> findByHabitIdAndDate(UUID habitId, LocalDate date);

    List<HabitEntry> findByHabitId(UUID habitId);

    List<HabitEntry> findByHabitIdAndDateBetween(UUID habitId, LocalDate startDate, LocalDate endDate);

    List<HabitEntry> findByHabitIdOrderByDateDesc(UUID habitId);

}
