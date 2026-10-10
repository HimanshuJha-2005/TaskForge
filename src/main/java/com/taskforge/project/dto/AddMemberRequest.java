package com.taskforge.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddMemberRequest(@Email @NotBlank String email) {}
