package com.taskforge.issue;

import com.taskforge.issue.dto.CommentRequest;
import com.taskforge.issue.dto.CommentResponse;
import com.taskforge.issue.dto.IssueRequest;
import com.taskforge.issue.dto.IssueResponse;
import com.taskforge.issue.dto.IssueStatusRequest;
import com.taskforge.issue.dto.IssueUpdateRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

  private final IssueService issueService;

  public IssueController(IssueService issueService) {
    this.issueService = issueService;
  }

  @GetMapping
  public ResponseEntity<com.taskforge.project.dto.PageResponse<IssueResponse>> search(
      Principal principal,
      @RequestParam Long projectId,
      @RequestParam(required = false) IssueStatus status,
      @RequestParam(required = false) Priority priority,
      @RequestParam(required = false) Long assigneeId,
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Page<IssueResponse> result =
        issueService.search(principal.getName(), projectId, status, priority, assigneeId, query, page, size);
    return ResponseEntity.ok(
        new com.taskforge.project.dto.PageResponse<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<IssueResponse> get(Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(issueService.get(principal.getName(), id));
  }

  @PostMapping
  public ResponseEntity<IssueResponse> create(
      Principal principal, @Valid @RequestBody IssueRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(issueService.create(principal.getName(), req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<IssueResponse> update(
      Principal principal, @PathVariable Long id, @Valid @RequestBody IssueUpdateRequest req) {
    return ResponseEntity.ok(issueService.update(principal.getName(), id, req));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<IssueResponse> changeStatus(
      Principal principal, @PathVariable Long id, @Valid @RequestBody IssueStatusRequest req) {
    return ResponseEntity.ok(issueService.changeStatus(principal.getName(), id, req));
  }

  @PostMapping("/{id}/comments")
  public ResponseEntity<CommentResponse> comment(
      Principal principal, @PathVariable Long id, @Valid @RequestBody CommentRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(issueService.comment(principal.getName(), id, req));
  }

  @GetMapping("/{id}/comments")
  public ResponseEntity<List<CommentResponse>> comments(Principal principal, @PathVariable Long id) {
    return ResponseEntity.ok(issueService.listComments(principal.getName(), id));
  }
}
