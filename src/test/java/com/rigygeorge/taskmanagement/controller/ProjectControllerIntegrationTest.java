package com.rigygeorge.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rigygeorge.taskmanagement.dto.CreateProjectRequest;
import com.rigygeorge.taskmanagement.dto.RegisterRequest;
import com.rigygeorge.taskmanagement.dto.UpdateProjectRequest;
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
class ProjectControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private String authToken;
    private String userId;
    
    @BeforeEach
    void setUp() throws Exception {
        // Register user and get token
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("projecttest@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Project");
        registerRequest.setLastName("Tester");
        registerRequest.setOrganizationName("Project Org");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String response = registerResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();
        userId = objectMapper.readTree(response).get("userId").asText();
    }
    
    // ============== PROJECT CRUD TESTS ==============
    
    @Test
    void createProject_ValidRequest_ReturnsCreated() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("Project Description");
        
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("Project Description"))
                .andExpect(jsonPath("$.createdBy").value(userId));
    }
    
    @Test
    void createProject_WithoutAuth_ReturnsForbidden() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("Description");
        
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void createProject_MissingName_ReturnsBadRequest() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        // Missing name
        request.setDescription("Description");
        
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getAllProjects_ReturnsProjectList() throws Exception {
        // Create a project first
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Test Description");
        
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // Get all projects
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }
    
    @Test
    void getProjectById_ValidProject_ReturnsProject() throws Exception {
        // Create a project
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Test Project");
        createRequest.setDescription("Test Description");
        
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // Get the project by ID
        mockMvc.perform(get("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }
    
    @Test
    void getProjectById_NonExistent_ReturnsNotFound() throws Exception {
        String fakeProjectId = "00000000-0000-0000-0000-000000000000";
        
        mockMvc.perform(get("/api/projects/" + fakeProjectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void updateProject_ValidRequest_ReturnsUpdated() throws Exception {
        // Create a project
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Original Name");
        createRequest.setDescription("Original Description");
        
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // Update the project
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");
        
        mockMvc.perform(put("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }
    
    @Test
    void deleteProject_ValidRequest_ReturnsNoContent() throws Exception {
        // Create a project
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Project to Delete");
        createRequest.setDescription("Will be deleted");
        
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // Delete the project
        mockMvc.perform(delete("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify it's deleted
        mockMvc.perform(get("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    // ============== MULTI-TENANCY TESTS ==============
    
    @Test
    void getProjects_DifferentTenant_CannotSeeOtherProjects() throws Exception {
        // Create project as first user
        CreateProjectRequest request1 = new CreateProjectRequest();
        request1.setName("Tenant 1 Project");
        request1.setDescription("First tenant project");
        
        mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        // Register second user (different tenant)
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("tenant2project@example.com");
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
        
        // Second user should not see first user's projects
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty()); // Should be empty
    }
    
    @Test
    void getProjectById_CrossTenant_ReturnsNotFound() throws Exception {
        // Create project as first user
        CreateProjectRequest request1 = new CreateProjectRequest();
        request1.setName("Tenant 1 Project");
        request1.setDescription("Private project");
        
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String projectResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // Register second user (different tenant)
        RegisterRequest register2 = new RegisterRequest();
        register2.setEmail("hacker@example.com");
        register2.setPassword("password123");
        register2.setFirstName("Hacker");
        register2.setLastName("User");
        register2.setOrganizationName("Evil Org");
        
        MvcResult register2Result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String tenant2Response = register2Result.getResponse().getContentAsString();
        String tenant2Token = objectMapper.readTree(tenant2Response).get("token").asText();
        
        // Second user tries to access first user's project
        mockMvc.perform(get("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound()); // Should not be able to access
    }
    
    // ============== FULL WORKFLOW TEST ==============
    
    @Test
    void projectWorkflow_CreateUpdateDelete_Success() throws Exception {
        // 1. Create project
        CreateProjectRequest createRequest = new CreateProjectRequest();
        createRequest.setName("Workflow Project");
        createRequest.setDescription("Workflow test");
        
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Workflow Project"))
                .andReturn();
        
        String projectResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(projectResponse).get("id").asText();
        
        // 2. Update project
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Updated Workflow");
        updateRequest.setDescription("Updated description");
        
        mockMvc.perform(put("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Workflow"));
        
        // 3. View in list
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Updated Workflow"));
        
        // 4. Delete project
        mockMvc.perform(delete("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // 5. Verify deletion
        mockMvc.perform(get("/api/projects/" + projectId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}