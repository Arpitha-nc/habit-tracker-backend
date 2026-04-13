package com.habit.tracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.habit.tracker.dto.request.CreateHabitRequest;
import com.habit.tracker.dto.request.UpdateHabitRequest;
import com.habit.tracker.dto.response.DashboardResponse;
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
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse createHabit(@Valid @RequestBody CreateHabitRequest request) {
        return habitService.createHabit(request);
    }

    @GetMapping
    public List<HabitResponse> getHabits(@PageableDefault(size = 100) Pageable pageable) {
        return habitService.getHabits(pageable);
    }

    @GetMapping("/{habitId}")
    public HabitResponse getHabit(@PathVariable UUID habitId) {
        return habitService.getHabit(habitId);
    }

    @PutMapping("/{habitId}")
    public HabitResponse updateHabit(@PathVariable UUID habitId,
            @Valid @RequestBody UpdateHabitRequest request) {
        return habitService.updateHabit(habitId, request);
    }

    @DeleteMapping("/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHabit(@PathVariable UUID habitId) {
        habitService.deleteHabit(habitId);
    }

    @PostMapping("/{habitId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeHabit(@PathVariable UUID habitId) {
        habitService.completeHabit(habitId);
    }

    @DeleteMapping("/{habitId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uncompleteHabit(@PathVariable UUID habitId) {
        habitService.uncompleteHabit(habitId);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return habitService.getDashboard();
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
