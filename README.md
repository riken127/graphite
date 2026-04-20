# Graphite Philosophy

## What Graphite Is

Graphite is a Java-first graph access library designed for developers who use Neo4j in modern applications and want a more expressive, type-safe, maintainable way to work with graph data.

Graphite does **not** try to replace graphs with relational thinking.
It embraces graph modelling, traversal, relationships, and pattern matching — while removing unnecessary friction from day-to-day Java development.

## The Core Problem

Many Java developers using Neo4j eventually fall into one of these paths:

* Writing raw Cypher strings across the codebase
* Manually managing `MATCH`, `MERGE`, aliases, and parameters
* Mixing persistence logic with business logic
* Losing refactor safety due to string-based queries
* Treating graph access as an escape hatch instead of a first-class model

Cypher is powerful, but raw query strings everywhere do not scale well in large codebases.

## The Graphite Belief

A graph database deserves a graph-native programming model.

Java developers should be able to express graph intent in code using fluent, composable, testable APIs — in the same spirit that collections gained power through Streams.

Instead of writing:

```java
MATCH (c:Consultant)-[:REFERRED_BY]->(m:Consultant)
WHERE c.id = $id
RETURN m
```

A developer should be able to express intent like:

```java
graphite
    .match(node(Consultant.class).as("c"))
    .out(REFERRED_BY)
    .to(node(Consultant.class).as("m"))
    .where(property("c", "id").eq(id))
    .select("m");
```

The concern is no longer syntax.
The concern becomes meaning.

## Design Principles

## 1. Java First

Graphite is designed for Java developers first.
The API should feel natural in Java, readable in Java, and maintainable in Java.

## 2. Graph Native

Graphs are not tables.
Relationships are first-class citizens.
Traversal matters.
Paths matter.
Direction matters.
Graphite respects that.

## 3. Type Safety Where Valuable

Use compile-time guarantees where they create real value:

* projections
* property references
* node metadata
* query structure

Avoid complexity when type safety becomes ceremony.

## 4. Escape Hatches Matter

No abstraction covers 100% of real use cases.
Graphite should always allow direct Cypher when needed.

## 5. Composition Over Magic

Queries should be assembled from small composable parts.
Avoid hidden runtime behaviour, surprising conventions, or excessive annotations.

## 6. Core Before Frameworks

The heart of Graphite is framework-independent.
Spring integration, Boot starters, repositories, and adapters come later.
The core model must stand alone.

## 7. Readability Is a Feature

A query should communicate intent clearly to future maintainers.
Readable persistence code is a competitive advantage.

## Non-Goals

Graphite does not aim to:

* Hide graph concepts behind relational abstractions
* Replace Cypher entirely
* Become a heavy ORM
* Generate accidental complexity through annotations and magic proxies
* Lock users into a proprietary runtime model

## Why the Name Graphite

Graphite is structured carbon.
Strong, elegant, layered, and useful.
It evokes writing, modelling, and precision.
A fitting metaphor for expressing graph systems cleanly.

## Long-Term Vision

Graphite should become the most natural way to use graph databases from Java:

* expressive queries
* clean architecture
* testable graph access
* framework integrations
* strong developer experience
* modern language design

## Final Principle

Databases should serve software design.
Software design should not be distorted by database syntax.

Graphite exists to restore that balance.

## Developer Setup

### Requirements

* JDK 21+
* Maven 3.8.7+

### Module Skeleton

* `graphite-bom`: published dependency-management BOM
* `graphite-core`: core graph API primitives
* `graphite-cypher`: Cypher-oriented query building utilities
* `graphite-metadata`: metadata and mapping models
* `graphite-spring`: Spring integration layer
* `graphite-spring-boot-starter`: starter entrypoint module
* `graphite-test`: shared test support utilities
* `graphite-examples`: minimal usage examples

### Core Package Layout

* `io.github.riken127.graphite.core.dsl`: fluent API entry points and builders
* `io.github.riken127.graphite.core.model`: immutable query AST
* `io.github.riken127.graphite.core.model.predicate`: predicate model types
* `io.github.riken127.graphite.core.validation`: query validation rules

### Cypher Package Layout

* `io.github.riken127.graphite.cypher.renderer`: query renderers and helpers
* `io.github.riken127.graphite.cypher.model`: rendered Cypher output model

### Style and Linting

* `.editorconfig` defines shared editor defaults
* `spotless-maven-plugin` enforces Google Java format and normalized POM/markdown formatting
* `maven-checkstyle-plugin` enforces `google_checks.xml`

### Common Commands

```bash
# format Java + POM + markdown files
mvn spotless:apply

# run compile, tests, style, and lint checks
mvn verify

# run tests for one module
mvn -pl graphite-core test
```

## MVP Query Flow

```java
MatchQuery query =
    Graphite.match(Graphite.node("Consultant").as("c"))
        .where(Graphite.property("c", "id").eq("123"))
        .where(Graphite.property("c", "skills").in(List.of("java", "neo4j")))
        .where(Graphite.property("c", "deletedAt").isNull())
        .select("c", "c.id")
        .orderBy(Graphite.desc("c", "createdAt"))
        .skip(0)
        .limit(25)
        .build();

RenderedQuery rendered = new MatchQueryRenderer().render(query);
```

## MVP Operations

```java
CreateQuery createQuery =
    Graphite.create(Graphite.node("Consultant").as("c"))
        .set("id", "123")
        .set("name", "Julia")
        .build();

MergeQuery mergeQuery =
    Graphite.merge(Graphite.node("Consultant").as("c"))
        .on("id", "123")
        .on("tenant", "acme")
        .onCreateSet("createdAt", "2026-04-20")
        .onMatchSet("lastSeen", "2026-04-20")
        .build();
```
