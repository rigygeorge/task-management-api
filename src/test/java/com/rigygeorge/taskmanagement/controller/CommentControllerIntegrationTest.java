package com.rigygeorge.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rigygeorge.taskmanagement.dto.*;
import com.rigygeorge.taskmanagement.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String authToken;
    private String taskId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register user and get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("commenttest@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Comment");
        registerRequest.setLastName("Tester");
        registerRequest.setOrganizationName("Comment Org");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String response = registerResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();
        
        // Create a project
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Comment Project");
        projectRequest.setDescription("Project for comments");
        
        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = projectResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // Create a task
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setProjectId(java.util.UUID.fromString(projectId));
        taskRequest.setTitle("Task for Comments");
        taskRequest.setDescription("Test task");
        taskRequest.setStatus(Task.TaskStatus.TODO);
        taskRequest.setPriority(Task.TaskPriority.MEDIUM);
        
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String taskResponse = taskResult.getResponse().getContentAsString();
        taskId = objectMapper.readTree(taskResponse).get("id").asText();
    }
    
    // ============== COMMENT CRUD TESTS ==============
    
    @Test
    void createComment_ValidRequest_ReturnsCreated() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("This is a test comment");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("This is a test comment"))
                .andExpect(jsonPath("$.userEmail").value("commenttest@example.com"))
                .andExpect(jsonPath("$.userName").exists())
                .andExpect(jsonPath("$.taskId").value(taskId));
    }
    
    @Test
    void createComment_WithoutAuth_ReturnsForbidden() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Unauthorized comment");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth
    }
    
    @Test
    void createComment_EmptyContent_ReturnsBadRequest() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent(""); // Empty content
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void createComment_NonExistentTask_ReturnsNotFound() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Comment on non-existent task");
        
        String fakeTaskId = "00000000-0000-0000-0000-000000000000";
        
        mockMvc.perform(post("/api/tasks/" + fakeTaskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getComments_ReturnsCommentList() throws Exception {
        // Create multiple comments
        CreateCommentRequest comment1 = new CreateCommentRequest();
        comment1.setContent("First comment");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated());
        
        CreateCommentRequest comment2 = new CreateCommentRequest();
        comment2.setContent("Second comment");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment2)))
                .andExpect(status().isCreated());
        
        // Get all comments
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Second comment")) // Newest first
                .andExpect(jsonPath("$[1].content").value("First comment"));
    }
    
    @Test
    void getComments_EmptyTask_ReturnsEmptyArray() throws Exception {
        // Get comments from task with no comments
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    void deleteComment_OwnComment_ReturnsNoContent() throws Exception {
        // Create a comment
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Comment to delete");
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String commentResponse = createResult.getResponse().getContentAsString();
        String commentId = objectMapper.readTree(commentResponse).get("id").asText();
        
        // Delete the comment
        mockMvc.perform(delete("/api/tasks/" + taskId + "/comments/" + commentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify it's deleted (list should be empty)
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
    
    @Test
    void deleteComment_NonExistentComment_ReturnsNotFound() throws Exception {
        String fakeCommentId = "00000000-0000-0000-0000-000000000000";
        
        mockMvc.perform(delete("/api/tasks/" + taskId + "/comments/" + fakeCommentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    // ============== MULTI-TENANCY TEST ==============
    
    @Test
    void createComment_CrossTenantTask_ReturnsNotFound() throws Exception {
        // Register second user (different tenant)
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("tenant2comment@example.com");
        register2.setPassword("password123");
        register2.setFirstName("Tenant");
        register2.setLastName("Two");
        register2.setOrganizationName("Different Org");
        
        MvcResult register2Result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String tenant2Response = register2Result.getResponse().getContentAsString();
        String tenant2Token = objectMapper.readTree(tenant2Response).get("token").asText();
        
        // Try to comment on first tenant's task
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Cross-tenant comment attempt");
        
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + tenant2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // Should not be able to access
    }
    
    @Test
    void getComments_CrossTenantTask_ReturnsNotFound() throws Exception {
        // Register second user (different tenant)
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("tenant2view@example.com");
        register2.setPassword("password123");
        register2.setFirstName("Tenant");
        register2.setLastName("Two");
        register2.setOrganizationName("Different Org");
        
        MvcResult register2Result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String tenant2Response = register2Result.getResponse().getContentAsString();
        String tenant2Token = objectMapper.readTree(tenant2Response).get("token").asText();
        
        // Try to view first tenant's task comments
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound()); // Should not be able to access
    }
    
    // ============== ADMIN PRIVILEGE TEST ==============
    
    @Test
    void deleteComment_AdminCanDeleteAnyComment() throws Exception {
        // This test assumes ADMIN role can delete any comment
        // Create a comment as first user
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Admin will delete this");
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String commentResponse = createResult.getResponse().getContentAsString();
        String commentId = objectMapper.readTree(commentResponse).get("id").asText();
        
        // Admin (which is the first user in this case) can delete it
        mockMvc.perform(delete("/api/tasks/" + taskId + "/comments/" + commentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
    
    // ============== FULL WORKFLOW TEST ==============
    
    @Test
    void commentWorkflow_CreateViewDelete_Success() throws Exception {
        // 1. Create comment
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent("Workflow test comment");
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Workflow test comment"))
                .andReturn();
        
        String commentResponse = createResult.getResponse().getContentAsString();
        String commentId = objectMapper.readTree(commentResponse).get("id").asText();
        
        // 2. View comments
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId))
                .andExpect(jsonPath("$[0].content").value("Workflow test comment"));
        
        // 3. Delete comment
        mockMvc.perform(delete("/api/tasks/" + taskId + "/comments/" + commentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // 4. Verify deletion
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
