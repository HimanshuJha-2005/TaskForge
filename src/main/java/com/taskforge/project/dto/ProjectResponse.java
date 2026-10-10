package com.taskforge.project.dto;

import java.util.List;

public record ProjectResponse(Long id, String name, String key, String description, List<String> members) {}
