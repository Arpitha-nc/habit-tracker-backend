package com.habit.tracker.dto.response;

import java.util.UUID;

public class HabitResponse {

    private UUID id;
    private String name;
    private String description;
    private int streak;

    public HabitResponse() {

    }

    public HabitResponse(UUID id, String name, String description, int streak) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.streak = streak;
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
}
