package com.taskforge.issue;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  @Query(
      """
      select i from Issue i where i.project.id = :projectId
        and (:status is null or i.status = :status)
        and (:priority is null or i.priority = :priority)
        and (:assigneeId is null or i.assignee.id = :assigneeId)
        and (:query is null or lower(i.title) like lower(concat('%', :query, '%')))
      """)
  Page<Issue> search(
      Long projectId,
      IssueStatus status,
      Priority priority,
      Long assigneeId,
      String query,
      Pageable pageable);

  @Query("select i from Issue i join fetch i.project where i.project.id in :projectIds order by i.id desc")
  List<Issue> findRecentByProjectIds(List<Long> projectIds, Pageable pageable);

  List<Issue> findByProjectId(Long projectId);
}
