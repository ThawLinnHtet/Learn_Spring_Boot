# SOLID Principles With Java And Spring Boot

This folder contains simple Java examples for the five SOLID principles.

Each principle has two code folders:

- `avoid/`: code that breaks the principle.
- `should/`: improved code that follows the principle.

Each principle also has a `note.md` file that explains the idea and how it applies to Java or Spring Boot projects.

## Study Order

1. Single Responsibility Principle
2. Open/Closed Principle
3. Liskov Substitution Principle
4. Interface Segregation Principle
5. Dependency Inversion Principle

## Why SOLID Matters In Spring Boot

SOLID helps you write code that is easier to change, test, and maintain.

In Spring Boot, SOLID usually appears through:

- Controllers that only handle HTTP requests and responses.
- Services that contain business logic.
- Repositories that handle data access.
- Interfaces for replaceable behavior.
- Constructor injection instead of creating dependencies manually with `new`.
