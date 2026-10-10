package com.taskforge.project;

import com.taskforge.auth.Role;
import com.taskforge.auth.User;
import com.taskforge.auth.UserRepository;
import com.taskforge.common.exception.ResourceNotFoundException;
import com.taskforge.project.dto.AddMemberRequest;
import com.taskforge.project.dto.ProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private final ProjectRepository projects;
  private final ProjectMemberRepository members;
  private final UserRepository users;

  public ProjectService(
      ProjectRepository projects, ProjectMemberRepository members, UserRepository users) {
    this.projects = projects;
    this.members = members;
    this.users = users;
  }

  /** Visible projects: owned/membership, or everything for ADMIN. */
  @Transactional(readOnly = true)
  public List<ProjectResponse> visibleTo(String email) {
    User user = findUser(email);
    List<Project> list;
    if (user.getRole() == Role.ADMIN) {
      list = projects.findAll();
    } else {
      List<Long> ids = members.findProjectIdsByUserId(user.getId());
      list = ids.isEmpty() ? List.of() : projects.findAllById(ids);
    }
    return list.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ProjectResponse get(String email, Long id) {
    Project project = findProject(id);
    requireMember(email, id);
    return toResponse(project);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
  public ProjectResponse create(String email, ProjectRequest req) {
    if (projects.existsByKeyIgnoreCase(req.key().trim())) {
      throw new IllegalStateException("Project key already exists");
    }
    User owner = findUser(email);
    Project saved =
        projects.save(
            new Project(req.name().trim(), req.key().trim(), req.description(), owner, Instant.now()));
    members.save(new ProjectMember(saved, owner));
    return toResponse(saved);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
  public ProjectResponse update(String email, Long id, ProjectRequest req) {
    Project project = findProject(id);
    requireMember(email, id);
    project.update(req.name().trim(), req.description());
    return toResponse(project);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
  public ProjectResponse addMember(String email, Long id, AddMemberRequest req) {
    Project project = findProject(id);
    requireMember(email, id);
    User newcomer =
        users.findByEmail(req.email().trim().toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!members.existsByProjectIdAndUserId(id, newcomer.getId())) {
      members.save(new ProjectMember(project, newcomer));
    }
    return toResponse(project);
  }

  /** Membership gate used by issue/comment flows: members + admins only. */
  @Transactional(readOnly = true)
  public void requireMember(String email, Long projectId) {
    User user = findUser(email);
    if (user.getRole() == Role.ADMIN) {
      return;
    }
    if (!projects.existsById(projectId)) {
      throw new ResourceNotFoundException("Project not found");
    }
    if (!members.existsByProjectIdAndUserId(projectId, user.getId())) {
      throw new SecurityException("Access denied");
    }
  }

  private Project findProject(Long id) {
    return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private User findUser(String email) {
    return users.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found"));
  }

  private ProjectResponse toResponse(Project project) {
    List<String> emails =
        members.findByProjectId(project.getId()).stream()
            .map(pm -> pm.getUser().getEmail())
            .sorted()
            .toList();
    return new ProjectResponse(
        project.getId(), project.getName(), project.getKey(), project.getDescription(), emails);
  }
}
