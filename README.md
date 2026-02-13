# Todo List Service

A RESTful API for managing a simple to-do list, built with Spring Boot 4 and Java 21.

## Prerequisites

- Java 21
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- Docker (for containerized run)

## Build & Run

### Local

```bash
# Build (compiles + generates API code from OpenAPI contract)
./mvnw clean package

# Run
./mvnw spring-boot:run
```

### Docker

```bash
# Build and run
docker compose up --build

# Stop
docker compose down
```

The application starts on port **8080**.

## Available URLs

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui/index.html | Swagger UI - interactive API documentation |
| http://localhost:8080/v3/api-docs | OpenAPI 3.0 JSON spec |
| http://localhost:8080/api/todos | Todo items API |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/todos` | Create a new todo item |
| GET | `/api/todos` | List todo items (defaults to not done only, use `?status=all` for all) |
| GET | `/api/todos/{id}` | Get a single todo item |
| PUT | `/api/todos/{id}` | Update a todo item description |
| PATCH | `/api/todos/{id}/done` | Mark a todo item as done |
| PATCH | `/api/todos/{id}/not-done` | Mark a todo item as not done |

## Project Structure

- `src/main/resources/openapi/api.yaml` - OpenAPI contract (source of truth)
- `target/generated-sources/openapi/` - Generated controller, delegate, and DTOs (do not edit)
- `src/main/java/org/com/maher/todo/` - Application code

## Tech Stack

- Spring Boot 4.0.2
- Java 21
- OpenAPI Generator (contract-first code generation with delegate pattern)
- Springdoc OpenAPI (Swagger UI)
- Maven
