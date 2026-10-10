package com.taskforge.auth.dto;

import com.taskforge.auth.Role;

public record AuthResponse(String token, String email, Role role) {}
