package com.habit.tracker.mapper;

import com.habit.tracker.dto.request.RegisterRequest;
import com.habit.tracker.entity.User;

public class UserMapper {

    public static User toEntity(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        return user;
    }

}
