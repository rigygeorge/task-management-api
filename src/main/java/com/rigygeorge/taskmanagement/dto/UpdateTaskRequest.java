package com.rigygeorge.taskmanagement.dto;

import com.rigygeorge.taskmanagement.entity.Task;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private Task.TaskStatus status;
    
    private Task.TaskPriority priority;
    
    private UUID assignedTo;
    
    private Instant dueDate;
}
