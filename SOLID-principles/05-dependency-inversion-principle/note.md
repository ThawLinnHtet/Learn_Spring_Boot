# Dependency Inversion Principle

## Meaning

High-level business code should depend on abstractions, not concrete low-level classes.

In simple words: a service should depend on an interface, not directly create its database class, email class, or external API client.

## Avoid

Avoid creating dependencies manually inside a service with `new`.

This tightly couples the service to one implementation and makes testing harder.

## Should

Create an interface for the dependency and inject it through the constructor.

In the example:

- `DipOrderService` depends on `DipOrderRepository`.
- `DipMySqlOrderRepository` is only one implementation.
- The service can be tested with a fake repository.

## Spring Boot Note

This is one of the most common SOLID principles in Spring Boot.

Spring Boot supports this pattern with constructor injection:

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

Do not use `new MySqlOrderRepository()` inside the service. Let Spring inject the dependency.
