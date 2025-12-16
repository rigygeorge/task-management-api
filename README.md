# Task Management API

Multi-tenant task management REST API built with Spring Boot 3.2.

## Features

- Multi-tenant data isolation
- User authentication (JWT - Coming in Day 2)
- Project and task management
- Role-based access control (RBAC)

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2
- **Database:** PostgreSQL 15
- **Migration:** Flyway
- **Build:** Maven
- **Containerization:** Docker

## Architecture
```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  Spring     │
│  Boot API   │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ PostgreSQL  │
└─────────────┘
```

## Database Schema

- **tenants:** Organization/company data
- **users:** User accounts with roles
- **projects:** Project information
- **tasks:** Task details with status and priority

## Setup Instructions

### Prerequisites

- Java 17
- Docker Desktop
- Maven 3.8+

### Run Locally

1. **Start PostgreSQL:**
```bash
   docker-compose up -d
```

2. **Run application:**
```bash
   ./mvnw spring-boot:run
```

3. **Test API:**
```
   http://localhost:8080/api/health
```

## Project Status

✅ Day 1 Complete:
- Database schema created
- Domain entities implemented
- Repositories configured
- Application running successfully

🚧 Coming Next (Day 2):
- JWT authentication
- User registration/login
- Security configuration

## API Endpoints

### Health Check
```
GET /api/health
```

**Response:**
```json
{
  "status": "UP",
  "message": "Task Management API is running",
  "timestamp": "2024-12-13T19:30:00Z"
}
```

## Development
```bash
# Run tests
./mvnw test

# Clean build
./mvnw clean install

# Run with debug
./mvnw spring-boot:run -Ddebug
```

## Database Access

**Connect to PostgreSQL:**
```bash
docker exec -it taskapi-postgres psql -U postgres -d taskmanagement
```

**Common commands:**
```sql
\dt          -- List tables
\d users     -- Describe users table
\q           -- Exit
```

## Author

Rigy George
- Email: rigythekkumpuram@gmail.com
- LinkedIn: [linkedin.com/in/rigy-george](https://linkedin.com/in/rigy-george-b2350aa7)
- GitHub: [github.com/rigygeorge](https://github.com/rigygeorge)

## License

This project is created for portfolio purposes.
