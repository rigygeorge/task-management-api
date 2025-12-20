package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.CreateTaskRequest;
import com.rigygeorge.taskmanagement.dto.TaskResponse;
import com.rigygeorge.taskmanagement.dto.UpdateTaskRequest;
import com.rigygeorge.taskmanagement.entity.Project;
import com.rigygeorge.taskmanagement.entity.Task;
import com.rigygeorge.taskmanagement.entity.User;
import com.rigygeorge.taskmanagement.exception.ResourceNotFoundException;
import com.rigygeorge.taskmanagement.repository.ProjectRepository;
import com.rigygeorge.taskmanagement.repository.TaskRepository;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private TaskService taskService;
    
    private CustomUserDetails adminUser;
    private CustomUserDetails memberUser;
    private CustomUserDetails otherTenantUser;
    
    private UUID tenantId;
    private UUID otherTenantId;
    private UUID projectId;
    private UUID taskId;
    
    private Project project;
    private Task task;
    
    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        otherTenantId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        
        // Create users with different roles
        adminUser = new CustomUserDetails(
            UUID.randomUUID(),
            tenantId,
            "admin@test.com",
            "password",
            "Admin",
            "User",
            User.Role.ADMIN
        );
        
        memberUser = new CustomUserDetails(
            UUID.randomUUID(),
            tenantId,
            "member@test.com",
            "password",
            "Member",
            "User",
            User.Role.MEMBER
        );
        
        // User from different tenant
        otherTenantUser = new CustomUserDetails(
            UUID.randomUUID(),
            otherTenantId,
            "other@test.com",
            "password",
            "Other",
            "User",
            User.Role.ADMIN
        );
        
        // Setup project
        project = new Project();
        project.setId(projectId);
        project.setTenantId(tenantId);
        project.setName("Test Project");
        
        // Setup task
        task = new Task();
        task.setId(taskId);
        task.setTenantId(tenantId);
        task.setProjectId(projectId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.TaskStatus.TODO);
        task.setPriority(Task.TaskPriority.MEDIUM);
        task.setAssignedTo(memberUser.getId());
        task.setCreatedBy(adminUser.getId());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    // ============== MULTI-TENANCY TESTS ==============
    
    @Test
    void createTask_CrossTenantProject_ThrowsResourceNotFoundException() {
        // Arrange: User from Tenant B tries to create task in Tenant A's project
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Project tenantAProject = new Project();
        tenantAProject.setId(projectId);
        tenantAProject.setTenantId(tenantId); // Different tenant!
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(tenantAProject));
        
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(projectId);
        request.setTitle("Malicious Task");
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request);
        });
        
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void createTask_ProjectNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(projectId);
        request.setTitle("New Task");
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(request);
        });
    }
    
    @Test
    void getTaskById_CrossTenantAccess_ThrowsResourceNotFoundException() {
        // Arrange: User from Tenant B tries to access Tenant A's task
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Task tenantATask = new Task();
        tenantATask.setId(taskId);
        tenantATask.setTenantId(tenantId); // Different tenant!
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(tenantATask));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(taskId);
        });
    }
    
    @Test
    void updateTask_CrossTenantAccess_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Task tenantATask = new Task();
        tenantATask.setId(taskId);
        tenantATask.setTenantId(tenantId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(tenantATask));
        
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Hacked Title");
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(taskId, request);
        });
        
        verify(taskRepository, never()).save(any());
    }
    
    @Test
    void deleteTask_CrossTenantAccess_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Task tenantATask = new Task();
        tenantATask.setId(taskId);
        tenantATask.setTenantId(tenantId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(tenantATask));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(taskId);
        });
        
        verify(taskRepository, never()).delete(any());
    }
    
    @Test
    void getAllTasks_OnlyReturnsSameTenantTasks() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        
        Task task1 = new Task();
        task1.setId(UUID.randomUUID());
        task1.setTenantId(tenantId);
        task1.setProjectId(projectId);
        task1.setTitle("Tenant A Task");
        task1.setStatus(Task.TaskStatus.TODO);
        task1.setPriority(Task.TaskPriority.MEDIUM);
        task1.setCreatedAt(Instant.now());
        task1.setUpdatedAt(Instant.now());
        
        when(taskRepository.findByTenantId(tenantId)).thenReturn(Arrays.asList(task1));
        
        // Act
        List<TaskResponse> result = taskService.getAllTasks(null, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tenant A Task", result.get(0).getTitle());
        verify(taskRepository).findByTenantId(tenantId);
    }
    
    @Test
    void getTasksByProject_CrossTenantProject_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Project tenantAProject = new Project();
        tenantAProject.setId(projectId);
        tenantAProject.setTenantId(tenantId);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(tenantAProject));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksByProject(projectId);
        });
    }
    
    // ============== BASIC CRUD TESTS ==============
    
    @Test
    void createTask_ValidRequest_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(projectId);
        request.setTitle("New Task");
        request.setDescription("Description");
        request.setPriority(Task.TaskPriority.HIGH);
        request.setStatus(Task.TaskStatus.TODO);
        
        // Act
        TaskResponse result = taskService.createTask(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void createTask_WithDefaults_SetsDefaultValues() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(projectId);
        request.setTitle("New Task");
        // No status or priority set - should default
        
        // Act
        TaskResponse result = taskService.createTask(request);
        
        // Assert
        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void getTaskById_ValidTask_ReturnsTask() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act
        TaskResponse result = taskService.getTaskById(taskId);
        
        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Test Task", result.getTitle());
    }
    
    @Test
    void getTaskById_TaskNotFound_ThrowsException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(taskId);
        });
    }
    
    @Test
    void updateTask_ValidRequest_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Title");
        request.setStatus(Task.TaskStatus.IN_PROGRESS);
        
        // Act
        TaskResponse result = taskService.updateTask(taskId, request);
        
        // Assert
        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    void deleteTask_ValidRequest_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act
        taskService.deleteTask(taskId);
        
        // Assert
        verify(taskRepository).delete(task);
    }
    
    // ============== FILTERING TESTS ==============
    
    @Test
    void getTasksByStatus_ReturnsFilteredTasks() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        
        Task todoTask = new Task();
        todoTask.setId(UUID.randomUUID());
        todoTask.setTenantId(tenantId);
        todoTask.setProjectId(projectId);
        todoTask.setStatus(Task.TaskStatus.TODO);
        todoTask.setTitle("TODO Task");
        todoTask.setPriority(Task.TaskPriority.MEDIUM);
        todoTask.setCreatedAt(Instant.now());
        todoTask.setUpdatedAt(Instant.now());
        
        when(taskRepository.findByTenantIdAndStatus(tenantId, Task.TaskStatus.TODO))
            .thenReturn(Arrays.asList(todoTask));
        
        // Act
        List<TaskResponse> result = taskService.getTasksByStatus(Task.TaskStatus.TODO);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.TaskStatus.TODO, result.get(0).getStatus());
        verify(taskRepository).findByTenantIdAndStatus(tenantId, Task.TaskStatus.TODO);
    }
    
    @Test
    void getTasksByPriority_ReturnsFilteredTasks() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        
        Task highPriorityTask = new Task();
        highPriorityTask.setId(UUID.randomUUID());
        highPriorityTask.setTenantId(tenantId);
        highPriorityTask.setProjectId(projectId);
        highPriorityTask.setStatus(Task.TaskStatus.TODO);
        highPriorityTask.setPriority(Task.TaskPriority.HIGH);
        highPriorityTask.setTitle("High Priority Task");
        highPriorityTask.setCreatedAt(Instant.now());
        highPriorityTask.setUpdatedAt(Instant.now());
        
        when(taskRepository.findByTenantIdAndPriority(tenantId, Task.TaskPriority.HIGH))
            .thenReturn(Arrays.asList(highPriorityTask));
        
        // Act
        List<TaskResponse> result = taskService.getTasksByPriority(Task.TaskPriority.HIGH);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Task.TaskPriority.HIGH, result.get(0).getPriority());
        verify(taskRepository).findByTenantIdAndPriority(tenantId, Task.TaskPriority.HIGH);
    }
    
    @Test
    void getMyTasks_ReturnsOnlyMyAssignedTasks() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(memberUser);
        
        Task myTask = new Task();
        myTask.setId(UUID.randomUUID());
        myTask.setTenantId(tenantId);
        myTask.setProjectId(projectId);
        myTask.setAssignedTo(memberUser.getId());
        myTask.setTitle("My Task");
        myTask.setStatus(Task.TaskStatus.TODO);
        myTask.setPriority(Task.TaskPriority.MEDIUM);
        myTask.setCreatedAt(Instant.now());
        myTask.setUpdatedAt(Instant.now());
        
        when(taskRepository.findByTenantIdAndAssignedTo(tenantId, memberUser.getId()))
            .thenReturn(Arrays.asList(myTask));
        
        // Act
        List<TaskResponse> result = taskService.getMyTasks();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(memberUser.getId(), result.get(0).getAssignedTo());
        verify(taskRepository).findByTenantIdAndAssignedTo(tenantId, memberUser.getId());
    }
    
    @Test
    void getTasksByProject_ValidProject_ReturnsProjectTasks() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        Task projectTask = new Task();
        projectTask.setId(UUID.randomUUID());
        projectTask.setTenantId(tenantId);
        projectTask.setProjectId(projectId);
        projectTask.setTitle("Project Task");
        projectTask.setStatus(Task.TaskStatus.TODO);
        projectTask.setPriority(Task.TaskPriority.MEDIUM);
        projectTask.setCreatedAt(Instant.now());
        projectTask.setUpdatedAt(Instant.now());
        
        when(taskRepository.findByProjectId(projectId))
            .thenReturn(Arrays.asList(projectTask));
        
        // Act
        List<TaskResponse> result = taskService.getTasksByProject(projectId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projectId, result.get(0).getProjectId());
    }
}