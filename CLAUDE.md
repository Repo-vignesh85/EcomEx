# CLAUDE.md

This file provides guidance to Claude Code when working in this repository.

## Project Overview

EcomEx — an e-commerce project.

## Architecture

EcomEx is built as a set of independently deployable Spring Boot microservices, one per business capability, each owning its own database (database-per-service). `order-service` is the reference implementation; new services should mirror its structure and conventions.

```
                        ┌─────────────┐
   client ───────────▶  │ API Gateway │
                        └──────┬──────┘
                               │ REST
        ┌───────────┬─────────┼─────────┬────────────┐
        ▼           ▼         ▼         ▼            ▼
   catalog-svc  cart-svc  order-svc  payment-svc  customer-svc
        │           │         │         │            │
        ▼           ▼         ▼         ▼            ▼
     catalog-db  cart-db   order-db  payment-db   customer-db

   order-svc ──(OrderCreatedEvent)──▶ message broker ──▶ inventory-svc, notification-svc
```

- **Synchronous** REST calls (via an API gateway) for client-facing request/response flows.
- **Asynchronous** events (immutable Java records, e.g. `OrderCreatedEvent`) for cross-service side effects, to avoid distributed transactions and keep services decoupled.
- No service accesses another service's database directly — only via its API or published events.

### Planned Services

| Service | Responsibility | Status |
|---|---|---|
| `order-service` | Order lifecycle: create, fetch, status transitions, cancellation | Scaffolded |
| `catalog-service` | Product/category data, search, pricing | Planned |
| `customer-service` | Customer profiles, addresses, auth identity | Planned |
| `cart-service` | Shopping cart contents, totals | Planned |
| `payment-service` | Payment intents, gateway integration, refunds | Planned |
| `inventory-service` | Stock levels, reservations, consumes `OrderCreatedEvent` | Planned |
| `notification-service` | Email/SMS on order/payment events | Planned |
| `api-gateway` | Routing, auth termination, rate limiting | Planned |

### Standard Tech Stack (per service)

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 4.0.5 |
| Persistence | Spring Data JPA, Hibernate, Liquibase (schema-as-code, no `ddl-auto`) |
| Database | PostgreSQL (staging/prod), H2 in-memory (dev) |
| Security | Spring Security |
| Object mapping | MapStruct |
| API docs | SpringDoc OpenAPI 3 / Swagger UI |
| Metrics | Micrometer + Prometheus |
| Logging | Logstash Logback Encoder (structured JSON) |
| Resilience | Spring Retry + AOP |
| Testing | JUnit 5, Spring Security Test, Testcontainers (Postgres) |
| Packaging | Docker, multi-stage build |

### Cross-Service Conventions

- Package convention: `com.skmcore.<service-name>`, layered as `config`, `controller`, `dto`, `event`, `exception`, `mapper`, `model`, `repository`, `service`/`service.impl`.
- **IDs**: UUID primary keys, DB/JPA-generated (`GenerationType.UUID`), never client-supplied.
- **Money**: `BigDecimal` with `precision = 19, scale = 2`.
- **Timestamps**: `Instant`, UTC, set via `@PrePersist`/`@PreUpdate` (`createdAt`, `updatedAt`), never client-writable.
- **API versioning**: REST paths prefixed `/api/v1/...`.
- **Status enums**: `@Enumerated(EnumType.STRING)` for migration-safe, human-readable DB values.
- **Errors**: shared `ErrorResponse` shape returned via a `@ControllerAdvice` `GlobalExceptionHandler`.
- **Profiles**: `default`/`dev` (H2, DEBUG), `staging` (Postgres, pool size 10, INFO), `prod` (Postgres, pool size 30, WARN). DB connection comes from `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` env vars in staging/prod — never hardcoded.
- **Observability**: every service exposes `/actuator/health` and `/actuator/prometheus`, tagged with `application=${spring.application.name}`.

### Open Decisions

- Message broker for events (Kafka vs RabbitMQ vs SQS) — `OrderCreatedEvent` exists as a record with no publisher wiring yet.
- API gateway technology (Spring Cloud Gateway vs external gateway).
- Auth model — centralized identity (OAuth2/JWT from `customer-service` or a dedicated auth server) vs. per-service checks.
- Service discovery — static config vs. Eureka/Consul/Kubernetes DNS.

### Adding a New Service — Checklist

1. Copy `order-service`'s structure: `pom.xml` (Spring Boot 4.0.5 parent, Java 25), `Dockerfile`, `.dockerignore`.
2. Package as `com.skmcore.<service-name>`.
3. Add a Liquibase changelog under `src/main/resources/db/changelog/`.
4. Wire `application.yml` with `default`/`staging`/`prod` profiles matching the conventions above.
5. Expose OpenAPI via `springdoc`, health/metrics via `actuator`.
6. Publish/consume events as immutable records under an `event` package.
7. Write a service `README.md` documenting endpoints, profiles, and local run instructions (see `order-service/README.md` as the template).

## General Guidelines

- Follow existing code style and conventions
- Keep changes focused and minimal
- Write clear commit messages
