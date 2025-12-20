package com.rigygeorge.taskmanagement.controller;

import com.rigygeorge.taskmanagement.dto.CreateTaskRequest;
import com.rigygeorge.taskmanagement.dto.TaskResponse;
import com.rigygeorge.taskmanagement.dto.UpdateTaskRequest;
import com.rigygeorge.taskmanagement.entity.Task;
import com.rigygeorge.taskmanagement.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tasks", description = "Task management endpoints - Create, update, delete, and filter tasks")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {
    
    private final TaskService taskService;
    
    @Operation(
        summary = "Create a new task",
        description = "Creates a new task within a project. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get all tasks",
        description = "Retrieves all tasks for the current tenant with optional filtering by status, priority, project, and assignee"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tasks retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing token"
        )
    })
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(@Parameter(description = "Filter by task status (TODO, IN_PROGRESS, DONE)")
    @RequestParam(required = false) Task.TaskStatus status,
    
    @Parameter(description = "Filter by task priority (LOW, MEDIUM, HIGH)")
    @RequestParam(required = false) Task.TaskPriority priority,
    
    @Parameter(description = "Filter by project ID")
    @RequestParam(required = false) UUID projectId,
    
    @Parameter(description = "Filter by assignee user ID")
    @RequestParam(required = false) UUID assigneeId) {
        List<TaskResponse> tasks = taskService.getAllTasks(status, priority, projectId, assigneeId);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get tasks by project",
        description = "Retrieves all tasks for a specific project"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@Parameter(description = "Project ID", required = true) 
                                                                @PathVariable UUID projectId) {
        List<TaskResponse> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }
    
    @Operation(
        summary = "Get task by ID",
        description = "Retrieves a single task by its ID. Only accessible to users within the same tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - task belongs to different tenant"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@Parameter(description = "Task ID", required = true) @PathVariable UUID id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    @Operation(
        summary = "Update task",
        description = "Updates an existing task. ADMIN can update any task, MANAGER can update tasks in their projects, MEMBER can only update their own tasks."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - cannot edit this task"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }
    
    @Operation(
        summary = "Delete task",
        description = "Deletes a task. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Task deleted successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found"
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.isManagerOrAdmin()")
    public ResponseEntity<Void> deleteTask(@Parameter(description = "Task ID", required = true) @PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(
        summary = "Get my assigned tasks",
        description = "Retrieves all tasks assigned to the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getMyTasks() {
        List<TaskResponse> tasks = taskService.getMyTasks();
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Get tasks by status",
        description = "Retrieves all tasks with a specific status (TODO, IN_PROGRESS, or DONE)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@Parameter(description = "Task status", required = true, example = "IN_PROGRESS")
                                                               @PathVariable Task.TaskStatus status) {
        List<TaskResponse> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
        summary = "Get tasks by priority",
        description = "Retrieves all tasks with a specific priority (LOW, MEDIUM, or HIGH)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponse>> getTasksByPriority(@Parameter(description = "Task priority", required = true, example = "HIGH")
                                                                 @PathVariable Task.TaskPriority priority) {
        List<TaskResponse> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }
}