package com.habit.tracker.dto.response;

public class DashboardResponse {

    private int totalHabits;
    private int completedToday;
    private int longestStreak;
    private int xp;
    private int level;

    public DashboardResponse(int totalHabits, int completedToday, int longestStreak, int xp, int level) {
        this.totalHabits = totalHabits;
        this.completedToday = completedToday;
        this.longestStreak = longestStreak;
        this.xp = xp;
        this.level = level;
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

}
