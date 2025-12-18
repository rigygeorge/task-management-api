package com.rigygeorge.taskmanagement.controller;

import com.rigygeorge.taskmanagement.dto.CommentResponse;
import com.rigygeorge.taskmanagement.dto.CreateCommentRequest;
import com.rigygeorge.taskmanagement.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@PathVariable UUID taskId) {
        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}