# Todo List Service

A RESTful API for managing a simple to-do list, built with Spring Boot 4 and Java 21.

## Service Description

The service provides full CRUD operations for todo items with automatic lifecycle management. A background scheduler periodically checks for overdue items and marks them as "past due", after which they become immutable via the API.

### Assumptions

- All timestamps are treated as UTC.
- Items with status "past due" cannot be modified or reverted through the API.
- The "get all not done" endpoint defaults to `NOT_DONE` items; pass `?status=all` to include all statuses.
- `dueDatetime` must be in the future at creation time.

## Tech Stack

- **Runtime:** Java 21
- **Framework:** Spring Boot 4.0.2
- **Persistence:** Spring Data JPA / Hibernate, H2 in-memory database
- **API:** OpenAPI Generator (contract-first with delegate pattern), Springdoc OpenAPI (Swagger UI)
- **Mapping:** MapStruct
- **Utilities:** Lombok
- **Build:** Maven

## Prerequisites

- Java 21
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- Docker (for containerized run)

## Build & Run

### Local

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

### Run Tests

```bash
./mvnw test
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
| http://localhost:8080/api/v1/todos | Todo items API |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/todos` | Create a new todo item |
| GET | `/api/v1/todos` | List todo items (defaults to not done only, use `?status=all` for all) |
| GET | `/api/v1/todos/{id}` | Get a single todo item |
| PUT | `/api/v1/todos/{id}` | Update a todo item description |
| PATCH | `/api/v1/todos/{id}/done` | Mark a todo item as done |
| PATCH | `/api/v1/todos/{id}/not-done` | Mark a todo item as not done |

## Project Structure

```
src/main/java/org/com/maher/todo/
├── TodoApplication.java
├── api/
│   └── TodosApiDelegateImpl.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── PastDueModificationException.java
│   └── TodoNotFoundException.java
├── mapper/
│   └── TodoItemMapper.java
├── model/
│   ├── TodoItem.java
│   └── TodoStatus.java
├── repository/
│   └── TodoItemRepository.java
├── scheduler/
│   └── PastDueScheduler.java
└── service/
    └── TodoService.java
```

- `src/main/resources/openapi/api.yaml` - OpenAPI contract (source of truth)
- `target/generated-sources/openapi/` - Generated controller, delegate, and DTOs (do not edit)