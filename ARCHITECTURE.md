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
graphite-neo4j Execution Adapter
      ↓
Neo4j Java Driver
      ↑
Spring / Spring Boot Integrations
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
* Alias validation and pattern scope
* MERGE / MATCH rendering strategies

## graphite-metadata

Maps Java types into graph metadata.

Responsibilities:

* node labels, IDs, and property names
* validated reflection descriptors for Java records
* thread-safe cached metadata lookup
* record construction and exact scalar coercion

## graphite-neo4j

Owns synchronous Neo4j execution without leaking driver result and summary implementations into the
rest of Graphite.

Responsibilities:

* Session and transaction lifecycle
* Read/write routing and query options
* Materialized result records, counters, summaries, and bookmarks
* Managed retryable and explicit transactions
* Driver-to-Graphite exception translation
* Node and relationship record mappers

## graphite-spring

Spring integration layer.

Responsibilities:

* Spring-aware `GraphiteSpringTemplate`
* Transaction participation through `GraphiteTransactionManager`
* Read-only routing, timeout propagation, rollback-only state, and transaction suspension

## graphite-spring-boot-starter

Auto-configuration and zero-friction setup.

Responsibilities:

* Externalized driver and query defaults
* Conditional, user-overridable runtime beans
* Optional startup connectivity verification
* Driver lifecycle management

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
5. Execution via `GraphiteClient` or `GraphiteSpringTemplate`
6. Materialized results and optional Java-record mapping

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

Internal implementations may evolve.
The user-facing DSL should remain predictable.

## Prefer immutable models

Records and immutable objects reduce accidental complexity.

## Prefer composition over inheritance

Query parts should combine cleanly.
Avoid deep class hierarchies.

## Renderer extension boundary

`CypherRenderer` is the public rendering facade. Built-in query models are handled by dedicated
`QueryRenderer<Q>` implementations. Additional query models can provide another renderer without
adding dispatch branches to the facade.

Path construction and MATCH-based writes share `PathPattern`, predicate validation, and match-clause
rendering so new operations do not each recreate graph traversal semantics.

## Current Boundary and Future Extensions

The structured AST deliberately supports a single connected path and fixed MATCH/CREATE/MERGE or
MATCH-based update/delete shapes. General clause pipelines and multiple pattern parts are not yet
represented. Raw Cypher is the supported compatibility boundary for queries beyond that subset.

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

Runtime integration tests cover operations, traversal, record mapping, transaction commit/rollback,
bookmarks, summaries, and failure translation. They skip when Docker is unavailable.

## Contract Tests

DSL input -> expected Cypher output.

## Final Rule

If a new feature weakens separation of layers, the feature should be redesigned.
