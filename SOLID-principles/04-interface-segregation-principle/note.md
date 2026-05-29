# Interface Segregation Principle

## Meaning

Classes should not be forced to implement methods they do not need.

It is better to have several small interfaces than one large interface with unrelated methods.

## Avoid

Avoid one big interface that forces every implementation to support everything.

For example, a robot worker can work, but it does not eat or sleep. If it must implement `eat()` and `sleep()`, the interface is too large.

## Should

Split large interfaces into focused interfaces:

- `IspWorkable` for work behavior.
- `IspEatable` for eat behavior.
- `IspSleepable` for sleep behavior.

Then each class implements only what it really supports.

## Spring Boot Note

In Spring Boot, keep service interfaces focused. Do not create one large service interface with many unrelated methods.

Small interfaces are easier to mock, test, and replace.
