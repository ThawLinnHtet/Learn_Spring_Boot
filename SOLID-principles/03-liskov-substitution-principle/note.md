# Liskov Substitution Principle

## Meaning

Child classes should be usable anywhere their parent class is expected without breaking the program.

If a subclass must throw an error for behavior defined by its parent, the inheritance design is probably wrong.

## Avoid

Avoid forcing every bird to have a `fly()` method. Some birds, like ostriches, cannot fly.

If `Ostrich` extends a class that promises `fly()`, then `Ostrich` breaks that promise.

## Should

Put shared behavior in a base type and special behavior in a more specific type.

In the example:

- `LspBird` has behavior common to all birds.
- `LspFlyingBird` is only for birds that can fly.
- `LspSparrow` can be passed anywhere a `LspFlyingBird` is required.
- `LspOstrich` is still a bird, but it is not treated as a flying bird.

## Spring Boot Note

In Spring Boot services, do not make subclasses or implementations throw `UnsupportedOperationException` for methods they cannot support.

Create smaller and more accurate abstractions instead.
