package com.habit.tracker.dto.response;

import java.time.LocalDate;

public class HeatmapResponse {

    private LocalDate date;
    private int count;

    public HeatmapResponse(LocalDate date, int count) {
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getCount() {
        return count;
    }
}