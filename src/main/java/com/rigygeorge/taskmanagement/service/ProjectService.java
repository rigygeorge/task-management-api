package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.CreateProjectRequest;
import com.rigygeorge.taskmanagement.dto.ProjectResponse;
import com.rigygeorge.taskmanagement.dto.UpdateProjectRequest;
import com.rigygeorge.taskmanagement.entity.Project;
import com.rigygeorge.taskmanagement.exception.ResourceNotFoundException;
import com.rigygeorge.taskmanagement.repository.ProjectRepository;
import com.rigygeorge.taskmanagement.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Project project = new Project();
        project.setTenantId(currentUser.getTenantId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedBy(currentUser.getId());
        
        project = projectRepository.save(project);
        
        return mapToResponse(project);
    }
    
    public List<ProjectResponse> getAllProjects() {
        CustomUserDetails currentUser = getCurrentUser();
        
        return projectRepository.findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public ProjectResponse getProjectById(UUID id) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Ensure project belongs to user's tenant
        if (!project.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        
        return mapToResponse(project);
    }
    
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Ensure project belongs to user's tenant
        if (!project.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        
        project = projectRepository.save(project);
        
        return mapToResponse(project);
    }
    
    @Transactional
    public void deleteProject(UUID id) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        // Ensure project belongs to user's tenant
        if (!project.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        
        projectRepository.delete(project);
    }
    
    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getCreatedBy(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }
}