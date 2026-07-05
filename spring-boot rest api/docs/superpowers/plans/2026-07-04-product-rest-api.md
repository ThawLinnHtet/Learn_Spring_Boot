# Product REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-grade Product REST API with PostgreSQL persistence, Redis cache-aside reads, MapStruct DTO mapping, validation, filtering, pagination, sorting, Docker Compose infrastructure, and tests.

**Architecture:** The API uses controller, service, repository/specification, mapper, DTO, common error, and cache configuration layers. The app runs locally with Maven while PostgreSQL and Redis run through Docker Compose. Tests use Testcontainers for isolated PostgreSQL and Redis services.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, PostgreSQL, Spring Data Redis, Bean Validation, MapStruct, JUnit 5, Mockito, MockMvc, Testcontainers, Docker Compose.

---

## File Structure

- Modify: `pom.xml` to add validation, Redis, MapStruct, compiler annotation processors, Mockito, and Redis Testcontainers support.
- Modify: `src/main/resources/application.properties` to configure PostgreSQL, Redis, JPA, and cache TTL for local runtime.
- Create: `docker-compose.yml` for local PostgreSQL and Redis.
- Create: `src/main/java/com/example/demo/product/Product.java` as the JPA entity.
- Create: `src/main/java/com/example/demo/product/ProductRepository.java` with JPA specification support.
- Create: `src/main/java/com/example/demo/product/ProductSpecification.java` for dynamic list filters.
- Create: `src/main/java/com/example/demo/product/ProductMapper.java` as the MapStruct mapper.
- Create: `src/main/java/com/example/demo/product/ProductService.java` for CRUD, filtering, sort validation, and cache-aside orchestration.
- Create: `src/main/java/com/example/demo/product/ProductController.java` for REST endpoints.
- Create: `src/main/java/com/example/demo/product/dto/ProductCreateRequest.java` for create validation.
- Create: `src/main/java/com/example/demo/product/dto/ProductUpdateRequest.java` for update validation.
- Create: `src/main/java/com/example/demo/product/dto/ProductResponse.java` as the API response contract.
- Create: `src/main/java/com/example/demo/product/dto/ProductSearchCriteria.java` for filter parameters.
- Create: `src/main/java/com/example/demo/common/ApiErrorResponse.java` for consistent error bodies.
- Create: `src/main/java/com/example/demo/common/PageResponse.java` for stable paginated API responses.
- Create: `src/main/java/com/example/demo/common/GlobalExceptionHandler.java` for centralized REST errors.
- Create: `src/main/java/com/example/demo/common/ResourceNotFoundException.java` for 404 errors.
- Create: `src/main/java/com/example/demo/config/CacheConfig.java` for Redis cache manager configuration.
- Modify: `src/test/java/com/example/demo/TestcontainersConfiguration.java` to provide PostgreSQL and Redis containers.
- Create: `src/test/java/com/example/demo/product/ProductRepositoryTest.java` for specification integration tests.
- Create: `src/test/java/com/example/demo/product/ProductServiceTest.java` for service/cache unit tests.
- Create: `src/test/java/com/example/demo/product/ProductControllerTest.java` for MVC validation and endpoint tests.
- Create: `src/test/java/com/example/demo/product/ProductIntegrationTest.java` for a full-stack smoke test.

## Task 1: Configure Dependencies And Local Infrastructure

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Modify: `src/test/java/com/example/demo/TestcontainersConfiguration.java`
- Create: `docker-compose.yml`

- [ ] **Step 1: Replace Maven configuration**

Use Spring Boot 4.1, Java 25, Redis, validation, MapStruct, PostgreSQL, Mockito, and Testcontainers. Configure Lombok and MapStruct annotation processing in one compiler plugin configuration.

- [ ] **Step 2: Configure local app properties**

Set `spring.datasource.url=jdbc:postgresql://localhost:5433/products_db`, `spring.datasource.username=postgres`, `spring.datasource.password=postgres`, `spring.data.redis.host=localhost`, `spring.data.redis.port=6379`, `spring.cache.type=redis`, and `app.cache.products-ttl=10m`.

- [ ] **Step 3: Add Docker Compose infrastructure**

Create PostgreSQL and Redis services with stable container names, exposed host ports `5433` and `6379`, and a named PostgreSQL volume.

- [ ] **Step 4: Add Redis Testcontainer support**

Update `TestcontainersConfiguration` to expose both `PostgreSQLContainer<?>` and `GenericContainer<?> redisContainer()` with `@ServiceConnection`.

## Task 2: Add Domain, DTOs, Mapper, Repository, And Error Types

**Files:**
- Create: `src/main/java/com/example/demo/product/Product.java`
- Create: `src/main/java/com/example/demo/product/ProductRepository.java`
- Create: `src/main/java/com/example/demo/product/ProductSpecification.java`
- Create: `src/main/java/com/example/demo/product/ProductMapper.java`
- Create: `src/main/java/com/example/demo/product/dto/ProductCreateRequest.java`
- Create: `src/main/java/com/example/demo/product/dto/ProductUpdateRequest.java`
- Create: `src/main/java/com/example/demo/product/dto/ProductResponse.java`
- Create: `src/main/java/com/example/demo/product/dto/ProductSearchCriteria.java`
- Create: `src/main/java/com/example/demo/common/ResourceNotFoundException.java`
- Create: `src/main/java/com/example/demo/common/ApiErrorResponse.java`
- Create: `src/main/java/com/example/demo/common/GlobalExceptionHandler.java`
- Create: `src/main/java/com/example/demo/config/CacheConfig.java`

- [ ] **Step 1: Add entity and DTO types**

Create a `Product` JPA entity with `id`, `name`, `description`, `price`, `quantity`, `createdAt`, and `updatedAt`. Add create/update request DTOs with Bean Validation and a serializable response DTO for Redis storage.

- [ ] **Step 2: Add repository and specifications**

Create `ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>`. Add specification methods for case-insensitive name contains, min/max price, and min/max quantity.

- [ ] **Step 3: Add MapStruct mapper**

Create a Spring component mapper with `toEntity(ProductCreateRequest)`, `updateEntity(ProductUpdateRequest, @MappingTarget Product)`, and `toResponse(Product)` methods. Ignore `id`, `createdAt`, and `updatedAt` when mapping request DTOs into entities.

- [ ] **Step 4: Add common error handling and cache config**

Add `ResourceNotFoundException`, `ApiErrorResponse`, `GlobalExceptionHandler`, and a Redis `CacheManager` that uses JSON serialization and `app.cache.products-ttl`.

## Task 3: Add Service And Controller

**Files:**
- Create: `src/main/java/com/example/demo/product/ProductService.java`
- Create: `src/main/java/com/example/demo/product/ProductController.java`

- [ ] **Step 1: Add service**

Implement create, find by id, search, update, and delete. Validate sort properties against `id`, `name`, `price`, `quantity`, `createdAt`, and `updatedAt`. Use Spring cache annotations for cache-aside: `@Cacheable` on find by id and `@CacheEvict` on update/delete.

- [ ] **Step 2: Add controller**

Expose `GET /api/products`, `GET /api/products/{id}`, `POST /api/products`, `PUT /api/products/{id}`, and `DELETE /api/products/{id}`. Use `@Valid` for request bodies and `@ModelAttribute` for search criteria.

## Task 4: Add Tests

**Files:**
- Create: `src/test/java/com/example/demo/product/ProductRepositoryTest.java`
- Create: `src/test/java/com/example/demo/product/ProductServiceTest.java`
- Create: `src/test/java/com/example/demo/product/ProductControllerTest.java`
- Create: `src/test/java/com/example/demo/product/ProductIntegrationTest.java`
- Modify: `src/test/java/com/example/demo/DemoApplicationTests.java`

- [ ] **Step 1: Add repository tests**

Verify specifications filter products by name, price range, and quantity range using PostgreSQL Testcontainers.

- [ ] **Step 2: Add service tests**

Verify create uses MapStruct and repository save, missing products throw `ResourceNotFoundException`, update evicts cache through annotations by using the proxied Spring service in integration coverage, and invalid sort fields throw `IllegalArgumentException`.

- [ ] **Step 3: Add controller tests**

Verify create validation returns `400`, create success returns `201` with `Location`, missing product returns `404`, and list returns paginated JSON.

- [ ] **Step 4: Add full integration smoke test**

Use MockMvc with PostgreSQL and Redis Testcontainers. Create a product, fetch it by id, update it, list products, and delete it.

## Task 5: Verify

**Files:**
- All implementation files from earlier tasks.

- [ ] **Step 1: Run tests**

Run: `./mvnw test`

Expected: Maven test phase completes successfully.

- [ ] **Step 2: Run compile/package check**

Run: `./mvnw -DskipTests package`

Expected: Maven package phase completes successfully.

- [ ] **Step 3: Optional local runtime check**

Run: `docker compose up -d postgres redis`, then `./mvnw spring-boot:run`.

Expected: Application starts and connects to PostgreSQL and Redis on localhost.

## Self-Review

- Spec coverage: The plan covers local app runtime, Docker-managed PostgreSQL and Redis, PostgreSQL persistence, Redis cache-aside, MapStruct mapping in both directions, validation, filtering, pagination, sorting, global error handling, and tests.
- Placeholder scan: The plan avoids deferred or undefined tasks. Implementation details are specific enough to execute directly in the current Spring Boot project.
- Type consistency: Product, DTO, mapper, repository, service, controller, error, cache, and test type names are consistent across tasks.
