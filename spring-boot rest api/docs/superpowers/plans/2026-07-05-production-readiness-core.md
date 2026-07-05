# Production Readiness Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add practical production-readiness improvements to the Product REST API: Flyway migrations, environment-driven config, Actuator health/readiness, Redis cache resilience, and real cache behavior tests.

**Architecture:** Keep the existing controller/service/repository layering. Add operational dependencies and configuration at the application boundary, use Flyway as schema owner, keep Redis as an optional cache optimization, and verify cache behavior through Spring-managed integration tests.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, PostgreSQL, Spring Data Redis, Spring Cache, Flyway, Spring Boot Actuator, JUnit 5, Mockito, Testcontainers.

---

## File Structure

- Modify: `pom.xml` to add `spring-boot-starter-actuator`, `flyway-core`, and `flyway-database-postgresql`.
- Modify: `src/main/resources/application.properties` to use environment-overridable local defaults, enable Flyway, switch JPA to `validate`, configure Redis timeout, and expose Actuator health/info.
- Create: `src/main/resources/db/migration/V1__create_products_table.sql` for explicit PostgreSQL schema creation.
- Modify: `src/main/java/com/example/demo/config/CacheConfig.java` to add a `CacheErrorHandler` and preserve Redis cache manager TTL/JSON serialization.
- Create: `src/test/java/com/example/demo/product/ProductCacheIntegrationTest.java` to prove cache hits and evictions using the proxied Spring service.

## Task 1: Add Operational Dependencies And Runtime Configuration

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Add dependencies**

Add these dependencies in `pom.xml` near the existing Spring Boot dependencies:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-core</artifactId>
</dependency>
<dependency>
	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

- [ ] **Step 2: Replace application properties**

Replace `src/main/resources/application.properties` with:

```properties
spring.application.name=demo

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5433/products_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

spring.flyway.enabled=true

spring.flyway.locations=classpath:db/migration

spring.flyway.baseline-on-migrate=true

spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.timeout=${REDIS_TIMEOUT:2s}
spring.cache.type=redis

app.cache.products-ttl=${PRODUCTS_CACHE_TTL:10m}

management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true
management.health.db.enabled=true
management.health.redis.enabled=true
```

- [ ] **Step 3: Run configuration compile check**

Run: `./mvnw.cmd -DskipTests compile`

Expected: build fails before Task 2 if Flyway cannot find the schema migration, or succeeds if Hibernate validation is not triggered during compile. Continue to Task 2 either way.

## Task 2: Add Flyway Schema Migration

**Files:**
- Create: `src/main/resources/db/migration/V1__create_products_table.sql`

- [ ] **Step 1: Add the migration**

Create `src/main/resources/db/migration/V1__create_products_table.sql` with:

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT products_price_non_negative CHECK (price >= 0),
    CONSTRAINT products_quantity_non_negative CHECK (quantity >= 0)
);
```

- [ ] **Step 2: Run context test**

Run: `./mvnw.cmd -Dtest=DemoApplicationTests test`

Expected: Spring context starts with Testcontainers PostgreSQL and Flyway migration applied.

## Task 3: Add Redis Cache Failure Handling

**Files:**
- Modify: `src/main/java/com/example/demo/config/CacheConfig.java`

- [ ] **Step 1: Add cache error handler**

Update `CacheConfig` to include a `CacheErrorHandler` bean. Keep the existing `RedisCacheManager` method unchanged except for imports.

```java
@Bean
CacheErrorHandler cacheErrorHandler() {
	return new CacheErrorHandler() {
		@Override
		public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		}

		@Override
		public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		}

		@Override
		public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		}

		@Override
		public void handleCacheClearError(RuntimeException exception, Cache cache) {
		}
	};
}
```

Required imports:

```java
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
```

- [ ] **Step 2: Run cache config compile check**

Run: `./mvnw.cmd -DskipTests compile`

Expected: compilation succeeds.

## Task 4: Add Cache Behavior Integration Tests

**Files:**
- Create: `src/test/java/com/example/demo/product/ProductCacheIntegrationTest.java`

- [ ] **Step 1: Write failing cache tests**

Create `ProductCacheIntegrationTest` with tests for cache hit, update eviction, and delete eviction:

```java
package com.example.demo.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.common.ResourceNotFoundException;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductUpdateRequest;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductCacheIntegrationTest {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		Cache cache = cacheManager.getCache("products");
		if (cache != null) {
			cache.clear();
		}
		productRepository.deleteAll();
	}

	@Test
	void secondFindByIdUsesCachedResponseWhenDatabaseRowChangesBehindCache() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));

		ProductResponse first = productService.findById(saved.getId());
		saved.setName("Changed Behind Cache");
		productRepository.saveAndFlush(saved);

		ProductResponse second = productService.findById(saved.getId());

		assertThat(first.name()).isEqualTo("Monitor");
		assertThat(second.name()).isEqualTo("Monitor");
	}

	@Test
	void updateEvictsCachedProduct() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));
		productService.findById(saved.getId());

		productService.update(saved.getId(), new ProductUpdateRequest("Updated Monitor", "32 inch", new BigDecimal("299.99"), 5));

		ProductResponse refreshed = productService.findById(saved.getId());
		assertThat(refreshed.name()).isEqualTo("Updated Monitor");
		assertThat(refreshed.description()).isEqualTo("32 inch");
		assertThat(refreshed.price()).isEqualByComparingTo("299.99");
		assertThat(refreshed.quantity()).isEqualTo(5);
	}

	@Test
	void deleteEvictsCachedProduct() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));
		productService.findById(saved.getId());

		productService.delete(saved.getId());

		assertThatThrownBy(() -> productService.findById(saved.getId()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Product not found: " + saved.getId());
	}

	private static Product product(String name, String description, String price, int quantity) {
		Product product = new Product();
		product.setName(name);
		product.setDescription(description);
		product.setPrice(new BigDecimal(price));
		product.setQuantity(quantity);
		return product;
	}
}
```

- [ ] **Step 2: Run the cache tests**

Run: `./mvnw.cmd -Dtest=ProductCacheIntegrationTest test`

Expected: tests pass once Tasks 1-3 are complete. If serialization or Flyway validation fails, fix the exact failing issue without changing API behavior.

## Task 5: Verify Full Practical-Core Upgrade

**Files:**
- All files touched above.

- [ ] **Step 1: Run full tests**

Run: `./mvnw.cmd test`

Expected: all tests pass.

- [ ] **Step 2: Run package check**

Run: `./mvnw.cmd -DskipTests package`

Expected: package phase succeeds.

- [ ] **Step 3: Inspect worktree**

Run: `git status --short`

Expected: only the production-readiness files and previously existing user changes are shown. Do not revert unrelated changes.

## Self-Review

- Spec coverage: Dependencies/config, Flyway schema, Actuator health/readiness, Redis cache resilience, cache behavior tests, and verification are covered.
- Placeholder scan: No deferred placeholders are present.
- Type consistency: File names, class names, property names, and cache name `products` match the existing codebase.
