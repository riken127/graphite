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
* Ordered clause and typed expression ASTs
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
* metadata-backed typed query references
* nested record construction, generic collections, and extensible value conversion

## graphite-neo4j

Owns synchronous Neo4j execution without leaking driver result and summary implementations into the
rest of Graphite.

Responsibilities:

* Session and transaction lifecycle
* Read/write routing and query options
* Materialized and closeable streaming results
* Query observation lifecycle hooks
* Idempotent schema operations
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
6. Materialized or streaming results and optional Java-record mapping

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

Path construction and MATCH-based writes share `PathPattern` and predicate validation. The general
`ClauseQuery` model composes independent match, filter, scope, unwind, mutation, procedure,
subquery, return, ordering, and paging clauses without adding dispatch branches to the renderer
facade. `UnionQuery` composes validated branches through its own renderer.

## Current Boundary and Future Extensions

The general AST covers common read and write pipelines, scoped subqueries, compatible unions, and
procedure calls with explicit read/write intent. The compatibility AST retains fixed
MATCH/CREATE/MERGE and MATCH-based update/delete shapes. Raw Cypher remains the boundary for
dynamic labels/types, expression comprehensions, procedure yield renaming, and vendor-specific
clauses.

Planned extension points:

* Query planners
* Reactive execution adapters
* Generated metamodels
* Versioned schema migrations
* Metrics-provider integrations
* Alternative graph backends

## Testing Strategy

## Unit Tests

Core and renderer logic.

## Integration Tests

Neo4j Testcontainers.

Runtime integration tests cover operations, structured write/subquery/union execution, traversal,
streaming completion/cancellation, record mapping, schema operations, transactions, bookmarks,
observations, and failure translation. Local runs skip when Docker is unavailable; CI sets
`graphite.requireDocker=true` so a missing daemon is a failure rather than a silent skip.

## Contract Tests

DSL input -> expected Cypher output.

## Final Rule

If a new feature weakens separation of layers, the feature should be redesigned.
