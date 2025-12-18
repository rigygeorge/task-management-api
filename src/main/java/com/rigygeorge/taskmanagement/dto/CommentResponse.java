package com.rigygeorge.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private String userEmail;
    private String userName;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
}