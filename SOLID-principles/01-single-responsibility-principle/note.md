# Single Responsibility Principle

## Meaning

A class should have only one reason to change.

This means one class should focus on one job. If a class validates data, saves data, and sends emails, it has too many responsibilities.

## Avoid

Avoid putting validation, persistence, and notification logic in one service class.

This makes the class harder to test and harder to change. For example, changing email logic should not require changing order validation code.

## Should

Split responsibilities into small focused classes:

- `SrpOrderValidator` validates an order.
- `SrpOrderRepository` saves an order.
- `SrpEmailNotifier` sends a confirmation email.
- `SrpOrderService` coordinates the workflow.

## Spring Boot Note

In Spring Boot, this commonly means:

- `@Controller` or `@RestController` handles web requests.
- `@Service` handles business logic.
- `@Repository` handles database access.
- Email, payment, and external API clients stay in their own classes.
