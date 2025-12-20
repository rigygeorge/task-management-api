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
class TaskControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String authToken;
    private String projectId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register and get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("tasktest@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Task");
        registerRequest.setLastName("Tester");
        registerRequest.setOrganizationName("Task Org");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String response = registerResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();
        
        // Create a project
        CreateProjectRequest projectRequest = new CreateProjectRequest();
        projectRequest.setName("Test Project");
        projectRequest.setDescription("Test Description");
        
        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = projectResult.getResponse().getContentAsString();
        projectId = objectMapper.readTree(projectResponse).get("id").asText();
    }
    
    // ============== TASK CRUD TESTS ==============
    
    @Test
    void createTask_ValidRequest_ReturnsCreated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(java.util.UUID.fromString(projectId));
        request.setTitle("New Task");
        request.setDescription("Task Description");
        request.setStatus(Task.TaskStatus.TODO);
        request.setPriority(Task.TaskPriority.HIGH);
        
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }
    
    @Test
    void createTask_WithoutAuth_ReturnsForbidden() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(java.util.UUID.fromString(projectId));
        request.setTitle("New Task");
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth
    }
    
    @Test
    void getAllTasks_ReturnsTaskList() throws Exception {
        // Create a task first
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(java.util.UUID.fromString(projectId));
        request.setTitle("Test Task");
        request.setStatus(Task.TaskStatus.TODO);
        request.setPriority(Task.TaskPriority.MEDIUM);
        
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Get all tasks
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }
    
    @Test
    void updateTask_ValidRequest_ReturnsUpdated() throws Exception {
        // Create a task
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setProjectId(java.util.UUID.fromString(projectId));
        createRequest.setTitle("Original Title");
        createRequest.setStatus(Task.TaskStatus.TODO);
        createRequest.setPriority(Task.TaskPriority.LOW);
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String taskResponse = createResult.getResponse().getContentAsString();
        String taskId = objectMapper.readTree(taskResponse).get("id").asText();
        
        // Update the task
        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setStatus(Task.TaskStatus.IN_PROGRESS);
        
        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
    
    @Test
    void deleteTask_ValidRequest_ReturnsNoContent() throws Exception {
        // Create a task
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setProjectId(java.util.UUID.fromString(projectId));
        createRequest.setTitle("Task to Delete");
        createRequest.setStatus(Task.TaskStatus.TODO);
        createRequest.setPriority(Task.TaskPriority.LOW);
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String taskResponse = createResult.getResponse().getContentAsString();
        String taskId = objectMapper.readTree(taskResponse).get("id").asText();
        
        // Delete the task
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify it's deleted
        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    // ============== MULTI-TENANCY TEST ==============
    
    @Test
    void getTasks_DifferentTenant_CannotSeeOtherTasks() throws Exception {
        // Create task as first user
        CreateTaskRequest request1 = new CreateTaskRequest();
        request1.setProjectId(java.util.UUID.fromString(projectId));
        request1.setTitle("Tenant 1 Task");
        request1.setStatus(Task.TaskStatus.TODO);
        request1.setPriority(Task.TaskPriority.MEDIUM);
        
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        // Register second user (different tenant)
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("tenant2@example.com");
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
        
        // Second user should not see first user's tasks
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty()); // Should be empty
    }
}