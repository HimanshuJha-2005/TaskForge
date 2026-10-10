package com.taskforge.project;

import com.taskforge.auth.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = "project_key"))
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  /** Short human key like "SHOP" used in issue lists. */
  @Column(name = "project_key", nullable = false, length = 12)
  private String key;

  @Column(length = 2000)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id")
  private User owner;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected Project() {}

  public Project(String name, String key, String description, User owner, Instant createdAt) {
    this.name = name;
    this.key = key;
    this.description = description;
    this.owner = owner;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getKey() {
    return key;
  }

  public String getDescription() {
    return description;
  }

  public User getOwner() {
    return owner;
  }

  public void update(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
