# Graphite Architecture

## Overview

Graphite is built in layers.
Each layer has a clear responsibility and minimal coupling.
This allows the project to evolve without turning into a framework monolith.

## Layered Model

```text
Application Code
      ↓
Graphite DSL API
      ↓
Query Model / AST
      ↓
Renderer (Cypher)
      ↓
Execution Adapter
      ↓
Neo4j Driver / Spring / Other Integrations
```

## Core Modules

## graphite-core

Contains the framework-independent heart of Graphite.

Responsibilities:

* Fluent query DSL
* Query AST
* Node / relationship patterns
* Expressions and predicates
* Projections
* Sorting / paging models
* Validation

No Spring. No driver code. No runtime magic.

## graphite-cypher

Responsible for converting Graphite queries into Cypher.

Responsibilities:

* Cypher rendering
* Parameter binding
* Alias generation
* MERGE / MATCH rendering strategies

## graphite-metadata

Maps Java types into graph metadata.

Responsibilities:

* Labels
* Relationship types
* IDs
* Property naming
* Reflection descriptors
* Cached metadata lookup

## graphite-spring

Spring integration layer.

Responsibilities:

* GraphiteTemplate
* Transaction participation
* Neo4jClient bridge
* Result mapping
* Exception translation

## graphite-spring-boot-starter

Auto-configuration and zero-friction setup.

## Internal Query Flow

A typical request follows this path:

```java
graphite
    .match(node(User.class).as("u"))
    .where(property("u", "id").eq(id))
    .select("u");
```

Becomes:

1. DSL builder objects
2. Immutable AST model
3. Validation pass
4. Cypher render output
5. Execution via adapter
6. Result mapping

## AST Philosophy

Graphite does not build strings directly.
It builds a structured query model first.

Why:

* easier testing
* safer transformations
* future optimizations
* alternate renderers possible
* clearer separation of concerns

## Design Constraints

## Core must compile without Spring

Framework adapters depend on core.
Never the inverse.

## Public API stability matters

n
Internal implementations may evolve.
The user-facing DSL should remain predictable.

## Prefer immutable models

Records and immutable objects reduce accidental complexity.

## Prefer composition over inheritance

Query parts should combine cleanly.
Avoid deep class hierarchies.

## Future Extensions

Planned extension points:

* Query planners
* Reactive execution adapters
* Generated metamodels
* Schema tooling
* Metrics / observability hooks
* Alternative graph backends

## Testing Strategy

## Unit Tests

Core and renderer logic.

## Integration Tests

Neo4j Testcontainers.

## Contract Tests

DSL input -> expected Cypher output.

## Final Rule

If a new feature weakens separation of layers, the feature should be redesigned.
