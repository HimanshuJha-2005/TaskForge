package com.taskforge.project;

import com.taskforge.issue.IssueService;
import com.taskforge.project.dto.AddMemberRequest;
import com.taskforge.project.dto.ProjectDashboard;
import com.taskforge.project.dto.ProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final IssueService issueService;

  public ProjectController(ProjectService projectService, IssueService issueService) {
    this.projectService = projectService;
    this.issueService = issueService;
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> list(Principal principal) {
    return ResponseEntity.ok(projectService.visibleTo(principal.getName()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponse> get(Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(projectService.get(principal.getName(), id));
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> create(
      Principal principal, @Valid @RequestBody ProjectRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.create(principal.getName(), req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectResponse> update(
      Principal principal, @PathVariable Long id, @Valid @RequestBody ProjectRequest req) {
    return ResponseEntity.ok(projectService.update(principal.getName(), id, req));
  }

  @PostMapping("/{id}/members")
  public ResponseEntity<ProjectResponse> addMember(
      Principal principal, @PathVariable Long id, @Valid @RequestBody AddMemberRequest req) {
    return ResponseEntity.ok(projectService.addMember(principal.getName(), id, req));
  }

  @GetMapping("/{id}/dashboard")
  public ResponseEntity<ProjectDashboard> dashboard(Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(issueService.dashboard(principal.getName(), id));
  }
}
