package com.rigygeorge.taskmanagement.repository;

import com.rigygeorge.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByTenantId(UUID tenantId);
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByAssignedTo(UUID assignedTo);
    List<Task> findByTenantIdAndStatus(UUID tenantId, Task.TaskStatus status);
    List<Task> findByTenantIdAndPriority(UUID tenantId, Task.TaskPriority priority);
    List<Task> findByTenantIdAndAssignedTo(UUID tenantId, UUID assignedTo);
}