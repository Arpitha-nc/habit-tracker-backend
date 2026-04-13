package com.habit.tracker.mapper;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.response.HabitResponse;
import com.habit.tracker.entity.Habit;

public class HabitMapper {

    public static Habit toEntity(CreateHabitRequest request) {

        Habit habit = new Habit();
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());

        return habit;
    }

    public static HabitResponse toResponse(Habit habit, int streak, boolean completedToday) {

        HabitResponse response = new HabitResponse();

        response.setId(habit.getId());
        response.setName(habit.getName());
        response.setDescription(habit.getDescription());
        response.setStreak(streak);
        response.setCompletedToday(completedToday);
        response.setCreatedAt(habit.getCreatedAt());

        return response;
    }

}
