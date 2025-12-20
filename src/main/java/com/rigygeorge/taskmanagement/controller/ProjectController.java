package com.rigygeorge.taskmanagement.controller;

import com.rigygeorge.taskmanagement.dto.CreateProjectRequest;
import com.rigygeorge.taskmanagement.dto.ProjectResponse;
import com.rigygeorge.taskmanagement.dto.UpdateProjectRequest;
import com.rigygeorge.taskmanagement.service.ProjectService;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;
import java.util.UUID;

@Tag(name = "Projects", description = "Project management operations - Create, update, and organize projects")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    
    @Operation(
        summary = "Create a new project",
        description = "Creates a new project in the current user's organization. All authenticated users can create projects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Project created successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request - missing required fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get all projects",
        description = "Retrieves all projects for the current user's organization (tenant). Projects from other tenants are not visible."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Projects retrieved successfully"
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required")
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    
    @Operation(
        summary = "Get project by ID",
        description = "Retrieves a single project by its unique identifier. Only accessible to users within the same tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project found",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - project belongs to different organization")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@Parameter(description = "Project ID", required = true) @PathVariable UUID id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }
    
    @Operation(
        summary = "Update project",
        description = "Updates an existing project. All authenticated users in the same tenant can update projects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Project updated successfully",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "404", description = "Project not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - project belongs to different organization")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(project);
    }
    
    @Operation(
        summary = "Delete project",
        description = "Deletes a project and all associated tasks. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN or MANAGER role required"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityUtils.isManagerOrAdmin()")
    public ResponseEntity<Void> deleteProject(@Parameter(description = "Project ID", required = true) @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
    
}