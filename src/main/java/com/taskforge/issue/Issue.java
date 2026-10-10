package com.taskforge.issue;

import com.taskforge.auth.User;
import com.taskforge.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "issues")
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id")
  private Project project;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(length = 4000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private IssueStatus status = IssueStatus.OPEN;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Priority priority;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignee_id")
  private User assignee;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_id")
  private User reporter;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected Issue() {}

  public Issue(
      Project project,
      String title,
      String description,
      Priority priority,
      User assignee,
      User reporter,
      Instant createdAt) {
    this.project = project;
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.assignee = assignee;
    this.reporter = reporter;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Project getProject() {
    return project;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public IssueStatus getStatus() {
    return status;
  }

  public void setStatus(IssueStatus status) {
    this.status = status;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public User getAssignee() {
    return assignee;
  }

  public void setAssignee(User assignee) {
    this.assignee = assignee;
  }

  public User getReporter() {
    return reporter;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void update(String title, String description, Priority priority) {
    this.title = title;
    this.description = description;
    this.priority = priority;
  }
}
