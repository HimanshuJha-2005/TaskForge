package com.taskforge.project.dto;

import com.taskforge.issue.IssueStatus;
import com.taskforge.issue.Priority;
import java.util.List;
import java.util.Map;

public record ProjectDashboard(
    long totalIssues,
    Map<IssueStatus, Long> byStatus,
    Map<Priority, Long> byPriority,
    Map<String, Long> openByAssignee,
    List<RecentIssue> recent) {
  public record RecentIssue(Long id, String title, IssueStatus status, Priority priority) {}
}
