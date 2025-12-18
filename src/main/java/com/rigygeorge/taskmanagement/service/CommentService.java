package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.CommentResponse;
import com.rigygeorge.taskmanagement.dto.CreateCommentRequest;
import com.rigygeorge.taskmanagement.entity.Comment;
import com.rigygeorge.taskmanagement.entity.Task;
import com.rigygeorge.taskmanagement.entity.User;
import com.rigygeorge.taskmanagement.exception.ResourceNotFoundException;
import com.rigygeorge.taskmanagement.repository.CommentRepository;
import com.rigygeorge.taskmanagement.repository.TaskRepository;
import com.rigygeorge.taskmanagement.repository.UserRepository;
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
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    
    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    @Transactional
    public CommentResponse createComment(UUID taskId, CreateCommentRequest request) {
        CustomUserDetails currentUser = getCurrentUser();
        
        // Verify task exists and belongs to user's tenant
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        
        Comment comment = new Comment();
        comment.setTenantId(currentUser.getTenantId());
        comment.setTaskId(taskId);
        comment.setUserId(currentUser.getId());
        comment.setContent(request.getContent());
        
        comment = commentRepository.save(comment);
        
        return mapToResponse(comment, currentUser.getEmail(), 
            currentUser.getFirstName() + " " + currentUser.getLastName());
    }
    
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        CustomUserDetails currentUser = getCurrentUser();
        
        // Verify task belongs to user's tenant
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        
        if (!task.getTenantId().equals(currentUser.getTenantId())) {
            throw new ResourceNotFoundException("Task not found");
        }
        
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(comment -> {
                    User user = userRepository.findById(comment.getUserId()).orElse(null);
                    String email = user != null ? user.getEmail() : "Unknown";
                    String name = user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown";
                    return mapToResponse(comment, email, name);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteComment(UUID commentId) {
        CustomUserDetails currentUser = getCurrentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        // Only comment author or admin can delete
        if (!comment.getUserId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
    }
    
    private CommentResponse mapToResponse(Comment comment, String email, String name) {
        return new CommentResponse(
            comment.getId(),
            comment.getTaskId(),
            comment.getUserId(),
            email,
            name,
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}