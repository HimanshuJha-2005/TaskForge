package com.taskforge.project;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  boolean existsByKeyIgnoreCase(String key);
}
