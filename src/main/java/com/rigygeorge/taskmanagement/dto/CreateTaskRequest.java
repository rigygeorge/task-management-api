package com.rigygeorge.taskmanagement.dto;

import com.rigygeorge.taskmanagement.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    
    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private Task.TaskStatus status = Task.TaskStatus.TODO;
    
    private Task.TaskPriority priority = Task.TaskPriority.MEDIUM;
    
    private UUID assignedTo;
    
    private Instant dueDate;
}
