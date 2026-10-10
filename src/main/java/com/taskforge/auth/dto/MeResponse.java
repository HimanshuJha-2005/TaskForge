package com.taskforge.auth.dto;

import com.taskforge.auth.Role;

public record MeResponse(String email, Role role) {}
