package com.taskforge.project;

import com.taskforge.auth.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  protected ProjectMember() {}

  public ProjectMember(Project project, User user) {
    this.project = project;
    this.user = user;
  }

  public Project getProject() {
    return project;
  }

  public User getUser() {
    return user;
  }
}
