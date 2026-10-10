package com.taskforge.project;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  List<ProjectMember> findByProjectId(Long projectId);

  @Query("select pm.project.id from ProjectMember pm where pm.user.id = :userId")
  List<Long> findProjectIdsByUserId(Long userId);
}
