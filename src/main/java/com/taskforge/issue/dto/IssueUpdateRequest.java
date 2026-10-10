package com.taskforge.issue.dto;

import com.taskforge.issue.IssueStatus;
import com.taskforge.issue.Priority;
import jakarta.validation.constraints.Size;

public record IssueUpdateRequest(
    @Size(max = 200) String title,
    @Size(max = 4000) String description,
    Priority priority,
    String assigneeEmail) {}
