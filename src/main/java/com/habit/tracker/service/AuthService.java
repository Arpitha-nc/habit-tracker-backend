package com.habit.tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.habit.tracker.dto.request.LoginRequest;
import com.habit.tracker.dto.request.RegisterRequest;
import com.habit.tracker.dto.request.UpdateProfileRequest;
import com.habit.tracker.dto.response.AuthResponse;
import com.habit.tracker.dto.response.UserProfileResponse;
import com.habit.tracker.entity.User;
import com.habit.tracker.exception.BadRequestException;
import com.habit.tracker.mapper.UserMapper;
import com.habit.tracker.repository.UserRepository;
import com.habit.tracker.security.JwtService;
import com.habit.tracker.util.CurrentUserUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail());
    }

    public UserProfileResponse getProfile() {
        User user = CurrentUserUtil.getCurrentUser();
        int level = user.getXp() / 100 + 1;
        return new UserProfileResponse(user.getName(), user.getEmail(), user.getXp(), level, user.getCreatedAt());
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = CurrentUserUtil.getCurrentUser();

        user.setName(request.getName());

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to set a new password");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        int level = user.getXp() / 100 + 1;
        return new UserProfileResponse(user.getName(), user.getEmail(), user.getXp(), level, user.getCreatedAt());
    }

}
