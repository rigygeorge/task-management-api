package com.rigygeorge.taskmanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rigygeorge.taskmanagement.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID>{

    
} 
