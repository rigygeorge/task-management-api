package com.rigygeorge.taskmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Multi-Tenant Task Management API",
        version = "1.0",
        description = """
            A production-ready REST API for task management with:
            - Multi-tenant data isolation
            - JWT-based authentication
            - Role-based access control (RBAC)
            - Task collaboration features
            
            **Authentication:**
            1. Register at `/api/auth/register` or login at `/api/auth/login`
            2. Copy the JWT token from response
            3. Click 'Authorize' button above and paste: `Bearer <your-token>`
            
            **Roles:**
            - ADMIN: Full access to all resources
            - MANAGER: Can manage projects and tasks
            - MEMBER: Can view and edit own tasks
            """,
        contact = @Contact(
            name = "Rigy George",
            email = "rigygeorge@example.com",
            url = "https://github.com/rigygeorge"
        )
    ),
    servers = {
        @Server(
            description = "Local Development",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Production",
            url = "https://your-app.railway.app"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    in = SecuritySchemeIn.HEADER,
    description = "Enter JWT token obtained from /api/auth/login"
)
public class OpenApiConfig {
}