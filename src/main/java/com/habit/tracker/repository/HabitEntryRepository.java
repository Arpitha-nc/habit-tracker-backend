package com.habit.tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.habit.tracker.entity.HabitEntry;

public interface HabitEntryRepository extends JpaRepository<HabitEntry, UUID> {

    Optional<HabitEntry> findByHabitIdAndDate(UUID habitId, LocalDate date);

    List<HabitEntry> findByHabitId(UUID habitId);

    List<HabitEntry> findByHabitIdAndDateBetween(UUID habitId, LocalDate startDate, LocalDate endDate);

    List<HabitEntry> findByHabitIdOrderByDateDesc(UUID habitId);

    @Query("SELECT e FROM HabitEntry e JOIN e.habit h WHERE h.user.id = :userId AND e.date BETWEEN :start AND :end")
    List<HabitEntry> findByUserIdAndDateBetween(@Param("userId") UUID userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(e) FROM HabitEntry e JOIN e.habit h WHERE h.user.id = :userId AND e.date = :date AND e.completed = true")
    long countCompletedByUserIdAndDate(@Param("userId") UUID userId, @Param("date") LocalDate date);

}
