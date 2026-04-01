package com.habit.tracker.dto.response;

import java.time.LocalDate;

public class WeeklyProgressResponse {

    private LocalDate date;
    private int completedHabits;

    public WeeklyProgressResponse() {

    }

    public WeeklyProgressResponse(LocalDate date, int completedHabits) {
        this.date = date;
        this.completedHabits = completedHabits;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getCompletedHabits() {
        return completedHabits;
    }

}
