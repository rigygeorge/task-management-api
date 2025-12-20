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
class CommentServiceTest {
    
    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private CommentService commentService;
    
    private CustomUserDetails currentUser;
    private CustomUserDetails otherTenantUser;
    private CustomUserDetails otherUser;
    private CustomUserDetails adminUser;
    
    private UUID tenantId;
    private UUID otherTenantId;
    private UUID taskId;
    private UUID commentId;
    
    private Task task;
    private Comment comment;
    private User userEntity;
    
    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        otherTenantId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        
        currentUser = new CustomUserDetails(
            UUID.randomUUID(),
            tenantId,
            "user@test.com",
            "password",
            "Test",
            "User",
            User.Role.MEMBER
        );
        
        otherUser = new CustomUserDetails(
            UUID.randomUUID(),
            tenantId,
            "other@test.com",
            "password",
            "Other",
            "User",
            User.Role.MEMBER
        );
        
        otherTenantUser = new CustomUserDetails(
            UUID.randomUUID(),
            otherTenantId,
            "hacker@test.com",
            "password",
            "Hacker",
            "User",
            User.Role.ADMIN
        );
        
        adminUser = new CustomUserDetails(
            UUID.randomUUID(),
            tenantId,
            "admin@test.com",
            "password",
            "Admin",
            "User",
            User.Role.ADMIN
        );
        
        userEntity = new User();
        userEntity.setId(currentUser.getId());
        userEntity.setTenantId(tenantId);
        userEntity.setEmail("user@test.com");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        
        task = new Task();
        task.setId(taskId);
        task.setTenantId(tenantId);
        task.setTitle("Test Task");
        
        comment = new Comment();
        comment.setId(commentId);
        comment.setTenantId(tenantId);
        comment.setTaskId(taskId);
        comment.setUserId(currentUser.getId());
        comment.setContent("Test comment");
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    // ============== MULTI-TENANCY TESTS ==============
    
    @Test
    void createComment_CrossTenantTask_ThrowsResourceNotFoundException() {
        // Arrange: User from Tenant B tries to comment on Tenant A's task
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Task tenantATask = new Task();
        tenantATask.setId(taskId);
        tenantATask.setTenantId(tenantId); // Different tenant!
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(tenantATask));
        
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Malicious comment");
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(taskId, request);
        });
        
        verify(commentRepository, never()).save(any());
    }
    
    @Test
    void createComment_TaskNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Comment");
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(taskId, request);
        });
    }
    
    @Test
    void getCommentsByTask_CrossTenantTask_ThrowsResourceNotFoundException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(otherTenantUser);
        
        Task tenantATask = new Task();
        tenantATask.setId(taskId);
        tenantATask.setTenantId(tenantId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(tenantATask));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentsByTask(taskId);
        });
    }
    
    // ============== OWNERSHIP TESTS ==============
    
    @Test
    void deleteComment_UserCanDeleteOwnComment() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        
        // Act
        commentService.deleteComment(commentId);
        
        // Assert
        verify(commentRepository).delete(comment);
    }
    
    @Test
    void deleteComment_UserCannotDeleteOthersComment_ThrowsRuntimeException() {
        // Arrange: Different MEMBER user tries to delete someone else's comment
        when(authentication.getPrincipal()).thenReturn(otherUser);
        
        Comment otherUsersComment = new Comment();
        otherUsersComment.setId(commentId);
        otherUsersComment.setUserId(currentUser.getId()); // Different owner
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(otherUsersComment));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(commentId);
        });
        
        assertEquals("You can only delete your own comments", exception.getMessage());
        verify(commentRepository, never()).delete(any());
    }
    
    @Test
    void deleteComment_AdminCanDeleteAnyComment() {
        // Arrange: Admin can delete anyone's comment
        when(authentication.getPrincipal()).thenReturn(adminUser);
        
        Comment someUsersComment = new Comment();
        someUsersComment.setId(commentId);
        someUsersComment.setUserId(currentUser.getId()); // Someone else's comment
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(someUsersComment));
        
        // Act
        commentService.deleteComment(commentId);
        
        // Assert
        verify(commentRepository).delete(someUsersComment);
    }
    
    @Test
    void deleteComment_CommentNotFound_ThrowsException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(commentId);
        });
    }
    
    // ============== BASIC CRUD TESTS ==============
    
    @Test
    void createComment_ValidRequest_Success() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Great work!");
        
        // Act
        CommentResponse result = commentService.createComment(taskId, request);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test comment", result.getContent());
        assertEquals(currentUser.getEmail(), result.getUserEmail());
        verify(commentRepository).save(any(Comment.class));
    }
    
    @Test
    void getCommentsByTask_ReturnsCommentsInOrder() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        Comment comment1 = new Comment();
        comment1.setId(UUID.randomUUID());
        comment1.setUserId(currentUser.getId());
        comment1.setContent("First");
        comment1.setCreatedAt(Instant.now().minusSeconds(7200));
        
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setUserId(currentUser.getId());
        comment2.setContent("Second");
        comment2.setCreatedAt(Instant.now().minusSeconds(3600));
        
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId))
            .thenReturn(Arrays.asList(comment2, comment1)); // Newest first
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(userEntity));
        
        // Act
        List<CommentResponse> result = commentService.getCommentsByTask(taskId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Second", result.get(0).getContent()); // Newest first
        assertEquals("First", result.get(1).getContent());
    }
    
    @Test
    void getCommentsByTask_UserNotFound_ReturnsUnknown() {
        // Arrange: Test when user is deleted but comments remain
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        Comment orphanComment = new Comment();
        orphanComment.setId(UUID.randomUUID());
        orphanComment.setUserId(UUID.randomUUID()); // User doesn't exist
        orphanComment.setContent("Orphan comment");
        orphanComment.setCreatedAt(Instant.now());
        
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId))
            .thenReturn(Arrays.asList(orphanComment));
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        
        // Act
        List<CommentResponse> result = commentService.getCommentsByTask(taskId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getUserEmail());
        assertEquals("Unknown", result.get(0).getUserName());
    }
    
    @Test
    void getCommentsByTask_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId))
            .thenReturn(Arrays.asList());
        
        // Act
        List<CommentResponse> result = commentService.getCommentsByTask(taskId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}