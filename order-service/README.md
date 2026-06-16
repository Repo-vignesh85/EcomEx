# Order Service

Spring Boot 4.x microservice for managing orders in the EcomEx e-commerce platform.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 25, Spring Boot 4.0.5 |
| Persistence | Spring Data JPA, Hibernate, Liquibase |
| Database | PostgreSQL (staging/prod), H2 (dev) |
| Security | Spring Security |
| Mapping | MapStruct 1.6.3 |
| API Docs | SpringDoc OpenAPI 3 |
| Metrics | Micrometer + Prometheus |
| Logging | Logstash Logback Encoder (structured JSON) |

## Prerequisites

- Java 25+
- Maven 3.9+
- Docker (for containerised builds and integration tests)

---

## Running Locally (H2 in-memory)

```bash
cd order-service
mvn spring-boot:run
```

| URL | Purpose |
|-----|---------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |
| http://localhost:8080/h2-console | H2 web console |
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/actuator/prometheus | Prometheus metrics |

## Running with Staging Profile

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=orderdb
export DB_USER=postgres
export DB_PASSWORD=secret

mvn spring-boot:run -Dspring-boot.run.profiles=staging
```

## Running Tests

```bash
mvn test
```

Integration tests use Testcontainers and require Docker to be running.

---

## Docker

### Build image

```bash
docker build -t order-service:latest .
```

### Run container

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_PORT=5432 \
  -e DB_NAME=orderdb \
  -e DB_USER=postgres \
  -e DB_PASSWORD=secret \
  order-service:latest
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/orders` | Create a new order |
| `GET` | `/api/v1/orders/{id}` | Get order by ID |
| `GET` | `/api/v1/orders/customer/{customerId}` | List orders by customer |
| `PATCH` | `/api/v1/orders/{id}/status?status=SHIPPED` | Update order status |
| `DELETE` | `/api/v1/orders/{id}` | Cancel an order |

### Order Status Values

`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`
`PENDING` → `CANCELLED` → `REFUNDED`

---

## Profiles

| Profile | Database | Connection Pool | Log Level |
|---------|----------|-----------------|-----------|
| `default` / `dev` | H2 in-memory | N/A | DEBUG |
| `staging` | PostgreSQL (env vars) | 10 | INFO |
| `prod` | PostgreSQL (env vars) | 30 | WARN |

### Environment Variables (staging / prod)

| Variable | Description |
|----------|-------------|
| `DB_HOST` | PostgreSQL hostname |
| `DB_PORT` | PostgreSQL port (default: 5432) |
| `DB_NAME` | Database name |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
