package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.CreateTaskRequest;
import com.rigygeorge.taskmanagement.dto.TaskResponse;
import com.rigygeorge.taskmanagement.dto.UpdateTaskRequest;
import com.rigygeorge.taskmanagement.entity.Project;
import com.rigygeorge.taskmanagement.entity.Task;
import com.rigygeorge.taskmanagement.exception.ResourceNotFoundException;
import com.rigygeorge.taskmanagement.repository.ProjectRepository;
import com.rigygeorge.taskmanagement.repository.TaskRepository;
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
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        
        // Verify project exists and belongs to user's tenant
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (!project.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Project not found");
        }
        
        Task task = new Task();
        task.setTenantId(currentUser.getTenantId());
        task.setProjectId(request.getProjectId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : Task.TaskPriority.MEDIUM);
        task.setAssignedTo(request.getAssignedTo());
        task.setDueDate(request.getDueDate());
        task.setCreatedBy(currentUser.getId());
        
        task = taskRepository.save(task);
        
        return mapToResponse(task);
    }
    
    public List<TaskResponse> getAllTasks() {
        CustomUserDetails currentUser = getCurrentUser();
        return taskRepository.findByTenantId(currentUser.getTenantId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        CustomUserDetails currentUser = getCurrentUser();
        
        // Verify project belongs to user's tenant
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (!project.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Project not found");
        }
        
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public TaskResponse getTaskById(UUID id) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        
        return mapToResponse(task);
    }
    
    @Transactional
    public TaskResponse updateTask(UUID id, UpdateTaskRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getAssignedTo() != null) task.setAssignedTo(request.getAssignedTo());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        
        task = taskRepository.save(task);
        
        return mapToResponse(task);
    }
    
    @Transactional
    public void deleteTask(UUID id) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        
        taskRepository.delete(task);
    }
    
    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getProjectId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getAssignedTo(),
            task.getCreatedBy(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}