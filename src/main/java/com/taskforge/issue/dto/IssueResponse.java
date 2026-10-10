package com.taskforge.issue.dto;

import com.taskforge.issue.IssueStatus;
import com.taskforge.issue.Priority;

public record IssueResponse(
    Long id,
    Long projectId,
    String projectKey,
    String title,
    String description,
    IssueStatus status,
    Priority priority,
    String assigneeEmail,
    String reporterEmail) {}
