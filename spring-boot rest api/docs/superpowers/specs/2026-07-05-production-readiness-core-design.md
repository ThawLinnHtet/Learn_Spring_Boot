# Production Readiness Core Design

## Goal

Improve the existing Product REST API from a local production-style demo into a safer practical baseline for production-like operation without adding broad platform features such as authentication, OpenAPI, tracing, or application containerization.

## Scope

This pass focuses on five areas:

- Explicit database schema management with Flyway.
- Environment-overridable runtime configuration.
- Operational health and readiness endpoints with Spring Boot Actuator.
- Redis cache resilience so cache failures do not break normal database-backed API requests.
- Cache behavior tests that prove cache hit and eviction behavior.

Out of scope for this pass:

- Authentication and authorization.
- OpenAPI/Swagger documentation.
- Structured logging, tracing, request IDs, and metrics dashboards.
- Dockerfile or Kubernetes deployment files for the Spring Boot application.
- Redis clustering, Sentinel, TLS, or managed cloud Redis deployment settings.
- Optimistic locking and advanced concurrency controls.

## Current State

The API already has Product CRUD endpoints, PostgreSQL persistence, Redis-backed Spring Cache for `ProductService.findById`, DTO validation, MapStruct mapping, global error responses, Docker Compose for PostgreSQL and Redis, and Testcontainers-based tests.

Redis caching currently uses `@Cacheable(cacheNames = "products", key = "#id")` for product-by-id reads and `@CacheEvict(cacheNames = "products", key = "#id")` on update and delete. Cached values are `ProductResponse` DTOs, which avoids serializing JPA entities.

The main production gaps are `spring.jpa.hibernate.ddl-auto=update`, hardcoded local credentials, no Actuator health/readiness endpoints, no cache-failure handling, and no tests proving real cache hits or eviction.

## Architecture

The current layered architecture stays intact. The production-readiness changes are additive and focused:

- `pom.xml` adds Flyway and Actuator dependencies.
- `application.properties` becomes environment-overridable and switches JPA schema handling to validation.
- `src/main/resources/db/migration/V1__create_products_table.sql` defines the initial `products` table.
- `CacheConfig` keeps Redis cache manager configuration and adds cache error handling.
- A new Redis client configuration adds command timeout settings through application properties.
- Integration tests verify cache behavior through the proxied Spring service and repository interaction.

No endpoint contract changes are required.

## Configuration Design

Local defaults continue to work with the existing Docker Compose services:

- `spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5433/products_db}`
- `spring.datasource.username=${DB_USERNAME:postgres}`
- `spring.datasource.password=${DB_PASSWORD:postgres}`
- `spring.data.redis.host=${REDIS_HOST:localhost}`
- `spring.data.redis.port=${REDIS_PORT:6379}`
- `spring.data.redis.timeout=${REDIS_TIMEOUT:2s}`
- `app.cache.products-ttl=${PRODUCTS_CACHE_TTL:10m}`

JPA schema mode changes to:

- `spring.jpa.hibernate.ddl-auto=validate`

Flyway owns schema creation and migration. This prevents Hibernate from silently changing production schema at startup.

Actuator configuration exposes health and info endpoints and enables readiness/liveness probes:

- `management.endpoints.web.exposure.include=health,info`
- `management.endpoint.health.probes.enabled=true`
- `management.health.redis.enabled=true`
- `management.health.db.enabled=true`

## Database Migration Design

Flyway migration `V1__create_products_table.sql` creates the `products` table matching the current JPA entity:

- `id BIGSERIAL PRIMARY KEY`
- `name VARCHAR(120) NOT NULL`
- `description VARCHAR(500)`
- `price NUMERIC(12, 2) NOT NULL`
- `quantity INTEGER NOT NULL`
- `created_at TIMESTAMP WITH TIME ZONE NOT NULL`
- `updated_at TIMESTAMP WITH TIME ZONE NOT NULL`

The migration includes simple constraints for non-negative `price` and `quantity`, mirroring API validation at the database layer.

## Redis Cache Resilience Design

Redis remains an optimization, not the source of truth. PostgreSQL remains authoritative.

The cache manager continues using JSON serialization and the configured TTL. A Spring `CacheErrorHandler` handles Redis get, put, evict, and clear failures by allowing the service method to continue instead of failing the request. This means:

- If Redis is unavailable during `GET /api/products/{id}`, the service reads from PostgreSQL and returns the product.
- If Redis is unavailable during update or delete eviction, the database operation still succeeds.
- A stale cache entry can remain if eviction fails, but this is accepted for this practical baseline because availability is prioritized over cache strictness.

The Redis command timeout is configured so a Redis outage does not hang requests for too long.

## Testing Design

Existing tests remain.

New cache-focused integration tests use Spring Boot with Testcontainers and the real proxied `ProductService` so Spring cache annotations are active. Tests verify:

- A second `findById` call for the same product uses the cache and does not hit the repository again.
- Updating a product evicts the cached value, so the next `findById` returns updated data.
- Deleting a product evicts the cached value, so the next `findById` returns not found.

The tests clear the `products` cache and database state before each test to avoid cross-test contamination.

## Success Criteria

The improvement is complete when:

- Maven tests pass with PostgreSQL and Redis Testcontainers.
- Application startup succeeds against Docker Compose PostgreSQL and Redis.
- Flyway creates the `products` table and Hibernate validates it.
- `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness` are available.
- Redis cache hit and eviction behavior is covered by tests.
- Database and Redis settings can be overridden with environment variables while local defaults still work.
