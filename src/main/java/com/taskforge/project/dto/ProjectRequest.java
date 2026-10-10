package com.taskforge.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Pattern(regexp = "[A-Z0-9]{2,12}", message = "must be 2-12 uppercase letters/digits") String key,
    @Size(max = 2000) String description) {}
