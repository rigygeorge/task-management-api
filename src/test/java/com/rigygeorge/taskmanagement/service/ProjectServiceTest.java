package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.CreateProjectRequest;
import com.rigygeorge.taskmanagement.dto.ProjectResponse;
import com.rigygeorge.taskmanagement.entity.Project;
import com.rigygeorge.taskmanagement.entity.User;
import com.rigygeorge.taskmanagement.exception.ResourceNotFoundException;
import com.rigygeorge.taskmanagement.repository.ProjectRepository;
import com.rigygeorge.taskmanagement.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private ProjectService projectService;
    
    private CustomUserDetails currentUser;
    private Project project;
    private UUID tenantId;
    private UUID userId;
    
    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        currentUser = new CustomUserDetails(
            userId,
            tenantId,
            "test@example.com",
            "password",
            "Test",
            "User",
            User.Role.ADMIN
        );
        
        project = new Project();
        project.setId(UUID.randomUUID());
        project.setTenantId(tenantId);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setCreatedBy(userId);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
    }
    
    @Test
    void createProject_Success() {
        // Arrange
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("New Description");
        
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        
        // Act
        ProjectResponse response = projectService.createProject(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(project.getId(), response.getId());
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    void getAllProjects_ReturnsUserProjects() {
        // Arrange
        List<Project> projects = Arrays.asList(project);
        when(projectRepository.findByTenantId(tenantId)).thenReturn(projects);
        
        // Act
        List<ProjectResponse> response = projectService.getAllProjects();
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(projectRepository).findByTenantId(tenantId);
    }
    
    @Test
    void getProjectById_ProjectNotFound_ThrowsException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(projectId);
        });
    }
}