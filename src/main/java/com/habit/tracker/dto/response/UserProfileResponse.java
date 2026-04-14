package com.habit.tracker.dto.response;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private String name;
    private String email;
    private int xp;
    private int level;
    private LocalDateTime memberSince;

    public UserProfileResponse(String name, String email, int xp, int level, LocalDateTime memberSince) {
        this.name = name;
        this.email = email;
        this.xp = xp;
        this.level = level;
        this.memberSince = memberSince;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public LocalDateTime getMemberSince() {
        return memberSince;
    }

}
