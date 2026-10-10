package com.taskforge.issue.dto;

import com.taskforge.issue.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record IssueStatusRequest(@NotNull IssueStatus status) {}
