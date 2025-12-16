package com.rigygeorge.taskmanagement.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rigygeorge.taskmanagement.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByTenantId(UUID tenantId);
}