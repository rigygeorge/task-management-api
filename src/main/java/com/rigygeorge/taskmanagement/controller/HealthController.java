package com.rigygeorge.taskmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Tag(
    name = "Health Check",
    description = "API health and status monitoring endpoint (no authentication required)"
)
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Operation(
        summary = "Check API health",
        description = """
            Returns the current health status of the API.
            This endpoint does not require authentication and can be used for:
            - Monitoring and alerting
            - Load balancer health checks
            - Deployment verification
            - Container orchestration probes
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "API is healthy and running",
        content = @Content(
            examples = @ExampleObject(
                name = "Healthy Response",
                value = """
                    {
                      "status": "UP",
                      "message": "Task Management API is running",
                      "timestamp": "2024-12-19T10:30:00.123Z"
                    }
                    """
            )
        )
    )
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Task Management API is running");
        response.put("timestamp", Instant.now());
        return response;
    }
}