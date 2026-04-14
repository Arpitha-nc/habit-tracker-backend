package com.habit.tracker.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.habit.tracker.entity.Habit;

public interface HabitRepository extends JpaRepository<Habit, UUID> {
    List<Habit> findByUserId(UUID userId);

    Page<Habit> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

}
