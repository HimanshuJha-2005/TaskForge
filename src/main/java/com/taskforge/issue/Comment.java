package com.taskforge.issue;

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
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "issue_id")
  private Issue issue;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false, length = 2000)
  private String body;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected Comment() {}

  public Comment(Issue issue, User author, String body, Instant createdAt) {
    this.issue = issue;
    this.author = author;
    this.body = body;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Issue getIssue() {
    return issue;
  }

  public User getAuthor() {
    return author;
  }

  public String getBody() {
    return body;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
