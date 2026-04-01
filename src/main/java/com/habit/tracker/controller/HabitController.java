package com.habit.tracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.response.HabitResponse;
import com.habit.tracker.dto.response.HeatmapResponse;
import com.habit.tracker.dto.response.WeeklyProgressResponse;
import com.habit.tracker.service.HabitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @PostMapping
    public HabitResponse createHabit(
            @Valid @RequestBody CreateHabitRequest request) {

        return habitService.createHabit(request);
    }

    @GetMapping
    public List<HabitResponse> getHabits() {

        return habitService.getHabits();
    }

    @DeleteMapping("/{habitId}")
    public void deleteHabit(@PathVariable UUID habitId) {

        habitService.deleteHabit(habitId);
    }

    @PostMapping("/{habitId}/complete")
    public void completeHabit(@PathVariable UUID habitId) {

        habitService.completeHabit(habitId);
    }

    @GetMapping("/progress/weekly")
    public List<WeeklyProgressResponse> getWeeklyProgress() {
        return habitService.getWeeklyProgress();
    }

    @GetMapping("/progress/heatmap")
    public List<HeatmapResponse> getHeatmap() {
        return habitService.getHeatmap();
    }

}