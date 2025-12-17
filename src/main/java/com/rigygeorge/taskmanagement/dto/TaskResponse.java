package com.rigygeorge.taskmanagement.dto;

import com.rigygeorge.taskmanagement.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private UUID assignedTo;
    private UUID createdBy;
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
