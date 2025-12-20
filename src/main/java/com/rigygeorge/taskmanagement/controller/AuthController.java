package com.rigygeorge.taskmanagement.controller;

import com.rigygeorge.taskmanagement.dto.AuthResponse;
import com.rigygeorge.taskmanagement.dto.LoginRequest;
import com.rigygeorge.taskmanagement.dto.RegisterRequest;
import com.rigygeorge.taskmanagement.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Authentication",
    description = "User registration and authentication endpoints. Start here to obtain JWT tokens for API access."
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
        summary = "Register a new user and organization",
        description = """
            Creates a new organization (tenant) and registers the first user as ADMIN.
            Returns a JWT token that should be used for subsequent API calls.
            
            **Note:** The first user in an organization is automatically assigned the ADMIN role.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Success",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                          "email": "admin@example.com",
                          "firstName": "John",
                          "lastName": "Doe",
                          "role": "ADMIN",
                          "tenantId": "660e8400-e29b-41d4-a716-446655440000"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing required fields or validation errors"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists - user is already registered"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
        summary = "Authenticate user",
        description = """
            Authenticates a user with email and password.
            Returns a JWT token that must be included in the Authorization header for protected endpoints.
            
            **Usage:**
            1. Call this endpoint with valid credentials
            2. Copy the token from the response
            3. Click 'Authorize' button above
            4. Enter: `Bearer <your-token>`
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Success",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "userId": "550e8400-e29b-41d4-a716-446655440000",
                          "email": "user@example.com",
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "role": "MEMBER",
                          "tenantId": "660e8400-e29b-41d4-a716-446655440000"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials - incorrect email or password"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing email or password"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
