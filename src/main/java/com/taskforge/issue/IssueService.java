package com.taskforge.issue;

import com.taskforge.auth.User;
import com.taskforge.auth.UserRepository;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.issue.dto.CommentRequest;
import com.taskforge.issue.dto.CommentResponse;
import com.taskforge.issue.dto.IssueRequest;
import com.taskforge.issue.dto.IssueResponse;
import com.taskforge.issue.dto.IssueStatusRequest;
import com.taskforge.issue.dto.IssueUpdateRequest;
import com.taskforge.project.Project;
import com.taskforge.project.ProjectRepository;
import com.taskforge.project.ProjectService;
import com.taskforge.project.dto.ProjectDashboard;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {

  private static final Map<IssueStatus, Set<IssueStatus>> TRANSITIONS = new EnumMap<>(IssueStatus.class);

  static {
    TRANSITIONS.put(IssueStatus.OPEN, EnumSet.of(IssueStatus.IN_PROGRESS));
    TRANSITIONS.put(IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.IN_REVIEW));
    TRANSITIONS.put(IssueStatus.IN_REVIEW, EnumSet.of(IssueStatus.IN_PROGRESS, IssueStatus.RESOLVED));
    TRANSITIONS.put(IssueStatus.RESOLVED, EnumSet.of(IssueStatus.IN_REVIEW, IssueStatus.CLOSED));
    TRANSITIONS.put(IssueStatus.CLOSED, EnumSet.noneOf(IssueStatus.class));
  }

  private final IssueRepository issues;
  private final CommentRepository comments;
  private final ProjectRepository projects;
  private final ProjectService projectService;
  private final UserRepository users;

  public IssueService(
      IssueRepository issues,
      CommentRepository comments,
      ProjectRepository projects,
      ProjectService projectService,
      UserRepository users) {
    this.issues = issues;
    this.comments = comments;
    this.projects = projects;
    this.projectService = projectService;
    this.users = users;
  }

  @Transactional(readOnly = true)
  public Page<IssueResponse> search(
      String email,
      Long projectId,
      IssueStatus status,
      Priority priority,
      Long assigneeId,
      String query,
      int page,
      int size) {
    projectService.requireMember(email, projectId);
    return issues
        .search(
            projectId,
            status,
            priority,
            assigneeId,
            query == null || query.isBlank() ? null : query.trim(),
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("id").descending()))
        .map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public IssueResponse get(String email, Long id) {
    Issue issue = findIssue(id);
    projectService.requireMember(email, issue.getProject().getId());
    return toResponse(issue);
  }

  @Transactional
  public IssueResponse create(String email, IssueRequest req) {
    projectService.requireMember(email, req.projectId());
    Project project = findProject(req.projectId());
    User reporter = findUser(email);
    User assignee =
        req.assigneeEmail() == null || req.assigneeEmail().isBlank()
            ? null
            : users.findByEmail(req.assigneeEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
    Issue saved =
        issues.save(
            new Issue(
                project,
                req.title().trim(),
                req.description(),
                req.priority(),
                assignee,
                reporter,
                Instant.now()));
    return toResponse(saved);
  }

  @Transactional
  public IssueResponse update(String email, Long id, IssueUpdateRequest req) {
    Issue issue = findIssue(id);
    projectService.requireMember(email, issue.getProject().getId());
    if (req.title() != null && !req.title().isBlank()) {
      issue.update(req.title().trim(), req.description(), req.priority() != null ? req.priority() : issue.getPriority());
    } else if (req.priority() != null || req.description() != null) {
      issue.update(issue.getTitle(), req.description() != null ? req.description() : issue.getDescription(),
          req.priority() != null ? req.priority() : issue.getPriority());
    }
    if (req.assigneeEmail() != null) {
      if (req.assigneeEmail().isBlank()) {
        issue.setAssignee(null);
      } else {
        User assignee =
            users.findByEmail(req.assigneeEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        issue.setAssignee(assignee);
      }
    }
    return toResponse(issue);
  }

  @Transactional
  public IssueResponse changeStatus(String email, Long id, IssueStatusRequest req) {
    Issue issue = findIssue(id);
    projectService.requireMember(email, issue.getProject().getId());
    if (!TRANSITIONS.get(issue.getStatus()).contains(req.status())) {
      throw new IssueTransitionException(
          "Illegal transition from " + issue.getStatus() + " to " + req.status());
    }
    issue.setStatus(req.status());
    return toResponse(issue);
  }

  @Transactional
  public CommentResponse comment(String email, Long issueId, CommentRequest req) {
    Issue issue = findIssue(issueId);
    projectService.requireMember(email, issue.getProject().getId());
    User author = findUser(email);
    Comment saved = comments.save(new Comment(issue, author, req.body().trim(), Instant.now()));
    return new CommentResponse(saved.getId(), author.getEmail(), saved.getBody());
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> listComments(String email, Long issueId) {
    Issue issue = findIssue(issueId);
    projectService.requireMember(email, issue.getProject().getId());
    return comments.findByIssueIdOrderByIdAsc(issueId).stream()
        .map(c -> new CommentResponse(c.getId(), c.getAuthor().getEmail(), c.getBody()))
        .toList();
  }

  /** Dashboard aggregates computed with Streams: group issues by status/priority/assignee. */
  @Transactional(readOnly = true)
  public ProjectDashboard dashboard(String email, Long projectId) {
    projectService.requireMember(email, projectId);
    List<Issue> all = issues.findByProjectId(projectId);

    Map<IssueStatus, Long> byStatus =
        all.stream().collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));
    Map<Priority, Long> byPriority =
        all.stream().collect(Collectors.groupingBy(Issue::getPriority, Collectors.counting()));
    Map<String, Long> openByAssignee =
        all.stream()
            .filter(i -> i.getStatus() != IssueStatus.CLOSED && i.getStatus() != IssueStatus.RESOLVED)
            .filter(i -> i.getAssignee() != null)
            .collect(Collectors.groupingBy(i -> i.getAssignee().getEmail(), Collectors.counting()));
    List<ProjectDashboard.RecentIssue> recent =
        all.stream()
            .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
            .limit(5)
            .map(i -> new ProjectDashboard.RecentIssue(i.getId(), i.getTitle(), i.getStatus(), i.getPriority()))
            .toList();
    return new ProjectDashboard(all.size(), byStatus, byPriority, openByAssignee, recent);
  }

  private Issue findIssue(Long id) {
    return issues.findById(id).orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
  }

  private Project findProject(Long id) {
    return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private User findUser(String email) {
    return users.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
  }

  private IssueResponse toResponse(Issue issue) {
    return new IssueResponse(
        issue.getId(),
        issue.getProject().getId(),
        issue.getProject().getKey(),
        issue.getTitle(),
        issue.getDescription(),
        issue.getStatus(),
        issue.getPriority(),
        issue.getAssignee() == null ? null : issue.getAssignee().getEmail(),
        issue.getReporter().getEmail());
  }
}
