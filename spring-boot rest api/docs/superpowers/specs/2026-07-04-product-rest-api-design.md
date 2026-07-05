# Product REST API Design

## Goal

Build a production-grade Product REST API in the current `spring-boot rest api` Spring Boot project. The application runs locally with Maven, while PostgreSQL and Redis run in Docker. PostgreSQL is the source of truth. Redis is used for product-by-id cache-aside reads.

## Runtime Model

- Run the Spring Boot application locally with `./mvnw spring-boot:run`.
- Run PostgreSQL and Redis with Docker Compose.
- PostgreSQL listens on `localhost:5433` to avoid conflicts with a local PostgreSQL service on the default port.
- Redis listens on `localhost:6379`.
- Automated tests use Testcontainers, so tests do not require the Docker Compose services to be running ahead of time.

## Architecture

The API uses a layered Spring Boot structure:

- `product` package contains the Product domain, repository, specification builder, mapper, service, and controller.
- `product.dto` contains request, response, and search DTOs.
- `common` contains shared API error response handling and domain exceptions.
- `config` contains Redis cache configuration.

Responsibilities are separated as follows:

- Controller handles HTTP request/response concerns only.
- Service handles business rules, persistence orchestration, allowed sorting validation, and cache-aside behavior.
- Repository handles database access and dynamic filtering through Spring Data JPA specifications.
- MapStruct handles DTO-to-entity and entity-to-DTO mapping.
- Global exception handling provides consistent error responses.

## API Endpoints

- `GET /api/products` returns a paginated product list with a stable API-owned pagination response shape.
- `GET /api/products/{id}` returns one product by id and uses Redis cache-aside.
- `POST /api/products` creates a product from a validated request body.
- `PUT /api/products/{id}` updates an existing product from a validated request body.
- `DELETE /api/products/{id}` deletes an existing product.

The list endpoint supports filters for product name, minimum and maximum price, and minimum and maximum quantity. It also supports pagination and sorting. Sorting is restricted to known product fields to avoid invalid or unsafe sort properties.

List responses use this stable shape instead of serializing Spring Data `Page` directly: `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`.

## Data Model

`Product` is a JPA entity with these fields:

- `id`
- `name`
- `description`
- `price`
- `quantity`
- `createdAt`
- `updatedAt`

Validation rules:

- `name` is required and has a bounded length.
- `description` is optional and has a bounded length.
- `price` is required and must be zero or positive.
- `quantity` is required and must be zero or positive.

## DTO Mapping

MapStruct is required for mapping in both directions:

- `ProductCreateRequest -> Product`
- `ProductUpdateRequest -> Product` for update-in-place mapping.
- `Product -> ProductResponse`

The service should not contain manual field-by-field DTO mapping. Update mapping must ignore immutable or server-managed fields such as `id`, `createdAt`, and `updatedAt`.

## Caching

The service uses cache-aside for `GET /api/products/{id}`:

- Read from Redis first.
- If absent, read from PostgreSQL.
- Store the response in Redis with a TTL.
- Evict the cached product on update and delete.

Redis stores product response data, not JPA entities, to avoid persistence proxy and serialization issues.

## Error Handling

The API returns consistent error responses through a global exception handler:

- `404 Not Found` for missing products.
- `400 Bad Request` for validation errors and invalid request parameters.
- `500 Internal Server Error` for unexpected failures.

Error responses include status, error, message, path, timestamp, and field-level validation details when applicable.

## Testing

The test suite covers the API at multiple levels:

- Repository tests use PostgreSQL Testcontainers for filtering behavior.
- Service tests use mocks for repository, mapper, and cache dependencies.
- Controller tests use MockMvc for request validation and response shape.
- Integration smoke tests use PostgreSQL and Redis Testcontainers to verify the full application path.

## Out Of Scope

- Containerizing the Spring Boot application.
- Authentication or authorization.
- Database migrations with Flyway or Liquibase.
- Multi-tenant behavior.
- Advanced Redis clustering or production deployment configuration.
