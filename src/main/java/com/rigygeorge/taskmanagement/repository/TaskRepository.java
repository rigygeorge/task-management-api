package com.rigygeorge.taskmanagement.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rigygeorge.taskmanagement.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByTenantId(UUID tenantId);
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByAssignedTo(UUID assignedTo);
}
