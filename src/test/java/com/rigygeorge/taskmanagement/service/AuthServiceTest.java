package com.rigygeorge.taskmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rigygeorge.taskmanagement.dto.AuthResponse;
import com.rigygeorge.taskmanagement.dto.RegisterRequest;
import com.rigygeorge.taskmanagement.entity.Tenant;
import com.rigygeorge.taskmanagement.entity.User;
import com.rigygeorge.taskmanagement.exception.ResourceAlreadyExistsException;
import com.rigygeorge.taskmanagement.repository.TenantRepository;
import com.rigygeorge.taskmanagement.repository.UserRepository;
import com.rigygeorge.taskmanagement.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TenantRepository tenantRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    private RegisterRequest registerRequest;
    private Tenant tenant;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setOrganizationName("Test Org");
        
        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("Test Org");
        
        user = new User();
        user.setId(UUID.randomUUID());
        user.setTenantId(tenant.getId());
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(User.Role.ADMIN);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(), any(), any(), any())).thenReturn("jwt_token");
        
        // Act
        AuthResponse response = authService.register(registerRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(tenantRepository).save(any(Tenant.class));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        
        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });
        
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
    
}
