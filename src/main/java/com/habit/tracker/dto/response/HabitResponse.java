package com.habit.tracker.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class HabitResponse {

    private UUID id;
    private String name;
    private String description;
    private int streak;
    private boolean completedToday;
    private LocalDateTime createdAt;

    public HabitResponse() {

    }

    public HabitResponse(UUID id, String name, String description, int streak, boolean completedToday, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.streak = streak;
        this.completedToday = completedToday;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getStreak() {
        return streak;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
