package com.rigygeorge.taskmanagement.service;

import com.rigygeorge.taskmanagement.dto.AuthResponse;
import com.rigygeorge.taskmanagement.dto.LoginRequest;
import com.rigygeorge.taskmanagement.dto.RegisterRequest;
import com.rigygeorge.taskmanagement.entity.Tenant;
import com.rigygeorge.taskmanagement.entity.User;
import com.rigygeorge.taskmanagement.exception.ResourceAlreadyExistsException;
import com.rigygeorge.taskmanagement.repository.TenantRepository;
import com.rigygeorge.taskmanagement.repository.UserRepository;
import com.rigygeorge.taskmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered. Please use a different email or login.");
        }
        
        // Create tenant (organization)
        Tenant tenant = new Tenant();
        tenant.setName(request.getOrganizationName());
        tenant = tenantRepository.save(tenant);
        
        // Create user
        User user = new User();
        user.setTenantId(tenant.getId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.ADMIN); // First user is admin
        
        user = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getTenantId()
        );
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        // Authenticate user (this will throw BadCredentialsException if wrong password)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        String token = jwtUtil.generateToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getTenantId()
        );
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }
}