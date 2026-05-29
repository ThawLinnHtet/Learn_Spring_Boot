# Open/Closed Principle

## Meaning

Software classes should be open for extension but closed for modification.

You should be able to add new behavior by adding new code, not by constantly editing old working code.

## Avoid

Avoid long `if`, `else if`, or `switch` blocks that must be changed every time a new business rule is added.

For example, a discount calculator should not need to be edited every time a new customer type is created.

## Should

Use an interface for replaceable behavior.

In the example:

- `OcpDiscountPolicy` defines the discount contract.
- Each customer type has its own discount policy class.
- `OcpDiscountCalculator` works with the interface, not hard-coded customer types.

## Spring Boot Note

In Spring Boot, you can inject a `List<OcpDiscountPolicy>` into a service. Spring can automatically provide all beans that implement the interface.

This lets you add a new policy class without changing the main calculator service.
