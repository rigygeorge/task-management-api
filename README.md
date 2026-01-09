# 🚀 Multi-Tenant Task Management API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Railway](https://img.shields.io/badge/Deployed%20on-Railway-blueviolet.svg)](https://railway.app/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> A production-ready REST API for multi-tenant task management with JWT authentication, role-based access control, and comprehensive test coverage.

## 🌟 Live Demo

- **API Base URL:** [https://task-management-api-production-2c22.up.railway.app](https://task-management-api-production-2c22.up.railway.app)
- **API Documentation (Swagger):** [https://task-management-api-production-2c22.up.railway.app/swagger-ui.html](https://task-management-api-production-2c22.up.railway.app/swagger-ui.html)
- **Health Check:** [https://task-management-api-production-2c22.up.railway.app/api/health](https://task-management-api-production-2c22.up.railway.app/api/health)

**Try it out:** Register a new account or create a test user in Swagger UI!

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [What I Learned](#-what-i-learned)

---

## ✨ Features

### Core Functionality
- ✅ **Multi-Tenant Architecture** - Complete data isolation between organizations
- ✅ **JWT Authentication** - Secure token-based authentication
- ✅ **Role-Based Access Control** - Three roles: Admin, Manager, Member
- ✅ **Project Management** - Create and organize projects within organizations
- ✅ **Task Management** - CRUD operations with status, priority, and assignment
- ✅ **Comments System** - Collaborate on tasks with threaded comments
- ✅ **Advanced Filtering** - Filter tasks by status, priority, project, and assignee

### Technical Highlights
- ✅ **80+ Comprehensive Tests** - Unit and integration tests with security focus
- ✅ **Multi-Tenant Security** - Row-level data isolation verified end-to-end
- ✅ **RESTful API Design** - 20+ well-documented endpoints
- ✅ **Interactive Documentation** - Swagger/OpenAPI specification
- ✅ **Docker Support** - Containerized with Docker Compose
- ✅ **Production Deployment** - Live on Railway with PostgreSQL

---

## 🏗️ Architecture

### High-Level Architecture

```
┌─────────────┐
│   Client    │
│ (Swagger UI)│
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────────────────────────────┐
│     Spring Boot Application         │
│                                     │
│  ┌──────────────────────────────┐  │
│  │   Security Layer (JWT)       │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│  ┌──────────▼───────────────────┐  │
│  │   Controller Layer           │  │
│  │   (REST Endpoints)           │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│  ┌──────────▼───────────────────┐  │
│  │   Service Layer              │  │
│  │   (Business Logic + RBAC)    │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│  ┌──────────▼───────────────────┐  │
│  │   Repository Layer (JPA)     │  │
│  └──────────┬───────────────────┘  │
└─────────────┼───────────────────────┘
              │ JDBC
              ▼
       ┌─────────────┐
       │ PostgreSQL  │
       │  Database   │
       └─────────────┘
```

### Multi-Tenancy Model

Every entity includes a `tenantId` for data isolation:

```java
@Entity
public class Task {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private UUID tenantId;  // Ensures data isolation
    
    // ... other fields
}
```

**Security verification:** Cross-tenant access returns `404 Not Found` instead of `403 Forbidden` to prevent information leakage.

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Modern Java features
- **Spring Boot 4.0.0** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **PostgreSQL** - Relational database
- **Flyway** - Database migrations
- **JWT (jjwt)** - Token-based authentication

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **80+ tests** covering:
  - Unit tests (34)
  - Integration tests (46)
  - Security tests (25+)

### Documentation & Tools
- **Swagger/OpenAPI 3** - API documentation
- **Maven** - Dependency management
- **Docker** - Containerization
- **Lombok** - Boilerplate reduction

### Deployment
- **Railway** - Cloud platform
- **PostgreSQL (Railway)** - Managed database
- **Docker** - Container runtime

---

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 15+ (or use Docker)
- Docker & Docker Compose (optional)

### Local Setup

1. **Clone the repository:**
```bash
git clone https://github.com/yourusername/taskmanagement.git
cd taskmanagement
```

2. **Configure database:**

Create a PostgreSQL database:
```sql
CREATE DATABASE taskmanagement;
```

Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskmanagement
    username: postgres
    password: your_password
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Docker Setup (Recommended)

```bash
# Start application with Docker Compose
docker-compose up --build

# Application runs on http://localhost:8080
# PostgreSQL runs on localhost:5432
```

---

## 📚 API Documentation

### Interactive Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Quick Start Guide

1. **Register a new organization:**
```bash
POST /api/auth/register
{
  "email": "admin@company.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "organizationName": "My Company"
}
```

2. **Login and get JWT token:**
```bash
POST /api/auth/login
{
  "email": "admin@company.com",
  "password": "password123"
}
```

3. **Use token in subsequent requests:**
```
Authorization: Bearer <your-jwt-token>
```

### Key Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new organization | No |
| POST | `/api/auth/login` | Login and get JWT | No |
| GET | `/api/projects` | List all projects | Yes |
| POST | `/api/projects` | Create new project | Yes |
| GET | `/api/tasks` | List all tasks (with filters) | Yes |
| POST | `/api/tasks` | Create new task | Yes (Manager+) |
| GET | `/api/tasks/my-tasks` | Get my assigned tasks | Yes |
| POST | `/api/tasks/{taskId}/comments` | Add comment | Yes |

**Full API documentation available in Swagger UI.**

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Test Coverage

- **Total Tests:** 80+
- **Unit Tests:** 34 (Service layer)
- **Integration Tests:** 46 (End-to-end)
- **Coverage Focus:** Security, RBAC, Multi-tenancy

### Key Test Scenarios

**Multi-Tenancy Security:**
```java
@Test
void getTaskById_CrossTenantAccess_ThrowsResourceNotFoundException() {
    // User from Tenant B cannot access Tenant A's task
    // Returns 404 (not 403) to avoid information leakage
}
```

**Role-Based Access Control:**
```java
@Test
void updateTask_Member_CanOnlyEditOwnTasks() {
    // Members can only modify tasks assigned to them
}
```

**End-to-End Workflows:**
```java
@Test
void taskWorkflow_CreateUpdateDelete_Success() {
    // Full lifecycle: Register → Create Project → Create Task → Update → Delete
}
```

---

## 🌐 Deployment

### Railway Deployment

The application is deployed on Railway with:
- Automatic deployments from GitHub
- Managed PostgreSQL database
- Environment variable configuration
- Health checks and monitoring

**Environment Variables:**
```bash
DATABASE_HOST=<railway-postgres-host>
DATABASE_PORT=5432
DATABASE_NAME=railway
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=<generated>
JWT_SECRET=<256-bit-secret>
JWT_EXPIRATION=86400000
HIBERNATE_DDL_AUTO=update
```

### Monitoring

- **Health Check:** `GET /api/health`
- **Metrics:** Available via Spring Boot Actuator (if enabled)
- **Logs:** Available in Railway dashboard

---

## 💡 What I Learned

### Technical Skills
- **Multi-tenancy architecture** - Implemented row-level data isolation with tenant filtering
- **Security-first approach** - Cross-tenant access prevention, RBAC, JWT authentication
- **Test-driven development** - Wrote 80+ tests covering security edge cases
- **Docker & deployment** - Containerized application with CI/CD pipeline
- **API design** - RESTful principles, proper HTTP status codes, consistent error handling

### Best Practices
- Separation of concerns (Controller → Service → Repository)
- DTO pattern for API contracts
- Global exception handling
- Environment-specific configuration
- Comprehensive API documentation

### Challenges Overcome
- **Multi-tenant data isolation:** Ensured queries always filter by `tenantId`
- **Security testing:** Verified cross-tenant access prevention at integration level
- **Role-based permissions:** Implemented fine-grained access control with custom security expressions
- **Database connection pooling:** Configured HikariCP for production workloads

---

## 📝 Future Enhancements

- [ ] Add pagination for large result sets
- [ ] Implement task attachments/file uploads
- [ ] Add email notifications for task assignments
- [ ] Create audit logging for compliance
- [ ] Add GraphQL API alongside REST
- [ ] Implement caching with Redis
- [ ] Add full-text search with Elasticsearch

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

Rigy Thekkumpuram George
- GitHub: [rigygeorge](https://github.com/rigygeorge)
- LinkedIn: [Rigy Thekkumpuram George](https://linkedin.com/in/rigy-george-b2350aa7)
- Email: rigythekkumpuram@gmail.com

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- Railway Platform
- PostgreSQL Community

---

**⭐ If you found this project helpful, please consider giving it a star!**