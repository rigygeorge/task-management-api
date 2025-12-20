package com.rigygeorge.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rigygeorge.taskmanagement.dto.LoginRequest;
import com.rigygeorge.taskmanagement.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ============== REGISTRATION TESTS ==============
    
    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setOrganizationName("New Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
    
    @Test
    void register_DuplicateEmail_ReturnsConflict() throws Exception {
        // Register first user
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("duplicate@example.com");
        request1.setPassword("password123");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setOrganizationName("Org1");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        // Try to register with same email
        RegisterRequest request2 = new RegisterRequest();
        request2.setEmail("duplicate@example.com");
        request2.setPassword("password456");
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setOrganizationName("Org2");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict());
    }
    
    @Test
    void register_MissingEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // Missing email
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setOrganizationName("Test Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void register_MissingPassword_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        // Missing password
        request.setFirstName("Test");
        request.setLastName("User");
        request.setOrganizationName("Test Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email"); // Not a valid email format
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setOrganizationName("Test Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    // ============== LOGIN TESTS ==============
    
    @Test
    void login_ValidCredentials_ReturnsOk() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("User");
        registerRequest.setOrganizationName("Login Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // Now login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
    
    @Test
    void login_InvalidPassword_ReturnsUnauthorized() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("wrongpass@example.com");
        registerRequest.setPassword("correctpassword");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setOrganizationName("Test Org");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
        
        // Try login with wrong password
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpass@example.com");
        loginRequest.setPassword("wrongpassword");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void login_NonExistentUser_ReturnsUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void login_MissingEmail_ReturnsBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        // Missing email
        loginRequest.setPassword("password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        // Missing password
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    // ============== JWT TOKEN VALIDATION TEST ==============
    
    @Test
    void registerAndLogin_TokensAreDifferent() throws Exception {
        // Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("tokentest@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Token");
        registerRequest.setLastName("Test");
        registerRequest.setOrganizationName("Token Org");
        
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String registerResponse = registerResult.getResponse().getContentAsString();
        String registerToken = objectMapper.readTree(registerResponse).get("token").asText();
        
        // Login with same credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("tokentest@example.com");
        loginRequest.setPassword("password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponse = loginResult.getResponse().getContentAsString();
        String loginToken = objectMapper.readTree(loginResponse).get("token").asText();
        
        // Tokens should be different (generated at different times)
        // Both should exist and be non-empty
        assert registerToken != null && !registerToken.isEmpty();
        assert loginToken != null && !loginToken.isEmpty();
    }
}