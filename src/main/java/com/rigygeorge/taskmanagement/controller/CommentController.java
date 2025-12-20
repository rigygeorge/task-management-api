package com.rigygeorge.taskmanagement.controller;

import com.rigygeorge.taskmanagement.dto.CommentResponse;
import com.rigygeorge.taskmanagement.dto.CreateCommentRequest;
import com.rigygeorge.taskmanagement.service.CommentService;

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

import java.util.List;
import java.util.UUID;

@Tag(name = "Comments", description = "Task comment management endpoints")
@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {
    
    private final CommentService commentService;
    
    @Operation(
        summary = "Add comment to task",
        description = "Creates a new comment on a task. All authenticated users can comment on tasks within their tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Comment created successfully",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - comment content is required"
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
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "Task ID to add comment to", required = true)
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Get all comments for a task",
        description = "Retrieves all comments for a specific task, ordered by creation date (newest first)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comments retrieved successfully"
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
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@Parameter(description = "Task ID", required = true) @PathVariable UUID taskId) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }
    
    @Operation(
        summary = "Delete comment",
        description = "Deletes a comment. Users can only delete their own comments, unless they are ADMIN."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Comment deleted successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - can only delete own comments"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Comment not found"
        )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Task ID", required = true)
            @PathVariable UUID taskId,
            @Parameter(description = "Comment ID to delete", required = true)
            @PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}