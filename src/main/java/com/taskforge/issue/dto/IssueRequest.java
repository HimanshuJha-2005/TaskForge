package com.taskforge.issue.dto;

import com.taskforge.issue.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueRequest(
    @NotNull Long projectId,
    @NotBlank @Size(max = 200) String title,
    @Size(max = 4000) String description,
    @NotNull Priority priority,
    String assigneeEmail) {}
