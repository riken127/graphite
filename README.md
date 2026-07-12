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
* `graphite-neo4j`: Neo4j Java Driver execution and transaction adapter
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

RenderedQuery rendered = new CypherRenderer().render(query);
```

### Relationship Traversal

```java
MatchQuery referrals =
    Graphite.match(Graphite.node("Consultant").as("c"))
        .out("REFERRED_BY")
        .as("ref")
        .hops(1, 3)
        .to(Graphite.node("Consultant").as("manager"))
        .where(
            Graphite.property("c", "active")
                .eq(true)
                .and(Graphite.property("manager", "name").startsWith("J")))
        .select("manager", "ref")
        .build();
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

Write-only create and merge operations can call `withoutReturn()` to avoid materializing results.

### Updates and Deletes

```java
UpdateQuery updateQuery =
    Graphite.match(Graphite.node("Consultant").as("c"))
        .where(Graphite.property("c", "id").eq("123"))
        .update()
        .set("name", "Julia")
        .remove("legacyName")
        .returning("c")
        .build();

DeleteQuery deleteQuery =
    Graphite.match(Graphite.node("Consultant").as("c"))
        .where(Graphite.property("c", "id").eq("123"))
        .detachDelete()
        .build();
```

### Raw Cypher Escape Hatch

Raw Cypher is explicit and keeps trusted query text separate from values:

```java
RawCypherQuery raw =
    new RawCypherQuery(
        "MATCH (n) WHERE n.id = $id RETURN n",
        Map.of("id", "123"));

RenderedQuery rendered = new CypherRenderer().render(raw);
```

Never concatenate untrusted values into raw Cypher. Use parameters for all values.

## Typed Clause Queries

`Graphite.query()` builds a general ordered clause AST alongside the compatibility builders. It
supports multiple patterns, `OPTIONAL MATCH`, typed expression predicates, `WITH`, `UNWIND`,
aliased or distinct projections, aggregate functions, expression sorting, and paging. The same AST
also supports relationship-aware `CREATE`/`MERGE`, `SET`, `REMOVE`, `DELETE`, procedure calls,
scoped subqueries, and compatible `UNION` branches.

```java
ReflectionNodeMetadataRegistry metadata = new ReflectionNodeMetadataRegistry();
GraphEntity<Consultant> consultant =
    new GraphEntityFactory(metadata).entity(Consultant.class, "c");

ClauseQuery query =
    Graphite.query()
        .match(Graphite.path(consultant.node()).build())
        .where(consultant.property("rating", Integer.class).gte(5))
        .with(
            Projection.of(consultant.variable()),
            Projection.as(consultant.property("name", String.class), "displayName"))
        .returning(
            true,
            Projection.of(Expressions.variable("displayName", String.class)))
        .orderBy(Expressions.asc(Expressions.variable("displayName", String.class)))
        .build();
```

The metadata-backed `GraphEntity` resolves `@GraphNode` and `@GraphProperty` values and verifies the
declared Java property type. For example, the property above is a `TypedPropertyRef<Integer>`, so
passing a string to `gte(...)` does not compile. The original string-based builders remain available
for source compatibility.

Write targets retain their Java type through `SetAssignment<T>`, while searched `CASE`, list, and
map expressions keep values parameterized:

```java
ClauseQuery update =
    Graphite.query()
        .match(Graphite.path(consultant.node()).build())
        .set(
            Expressions.set(
                consultant.property("status", String.class),
                Expressions.caseWhen(
                    String.class,
                    Expressions.value("inactive", String.class),
                    Expressions.when(
                        consultant.property("rating", Integer.class).gte(5),
                        Expressions.value("preferred", String.class)))))
        .build();
```

Use `Graphite.subquery(imports...)` to validate imported scope before attaching it with
`subquery(...)`. `Graphite.union(...)` and `unionAll(...)` require every branch to return the same
explicitly aliased columns. Procedure calls default to write routing; use `callReadOnly(...)` only
when the procedure's read-only behavior is known.

## Neo4j Execution

`graphite-neo4j` executes both structured queries and the explicit raw-Cypher escape hatch. The
application owns the Neo4j `Driver`; closing a `GraphiteClient` is therefore unnecessary and does
not close the driver.

```java
try (Driver driver =
    GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", password))) {
  GraphiteClient client = new GraphiteClient(driver);

  QueryResult<String> result =
      client.execute(query, record -> record.value("c.id").asString());

  String id = result.single();
  QueryCounters counters = result.summary().counters();
  Set<String> bookmarks = result.bookmarks();
}
```

`QueryOptions` configures database selection, read/write routing, transaction timeout and metadata,
fetch size, causal bookmarks, and user impersonation. Raw Cypher defaults to write routing when its
access mode is `AUTO`, because Graphite cannot safely infer whether arbitrary text mutates data.

Managed transactions use the driver's retryable transaction functions:

```java
TransactionResult<String> created =
    client.writeTransaction(
        tx -> tx.execute(createQuery, record -> record.value("c.id").asString()).single());
```

For framework integration or manual transaction control, use `beginTransaction(...)` in a
try-with-resources block. An active transaction is rolled back when closed without a commit.

### Streaming Results

Use a closeable stream for large result sets. Exhausting it commits its read transaction and makes
the summary and bookmarks available. Closing it early rolls the transaction back and releases the
driver session.

```java
try (StreamingQueryResult<Consultant> results =
    client.stream(query, Neo4jMappers.node("c", Consultant.class, entities))) {
  results.forEachRemaining(this::process);
  QuerySummary summary = results.summary().orElseThrow();
}
```

`StreamingQueryResult` is single-use and not thread-safe. Always use try-with-resources, including
when converting it to a Java `Stream`. `GraphiteSpringTemplate.stream(...)` is available outside
`@Transactional`; a stream owns its transaction and is rejected inside a Spring-managed one.

## Record Mapping

The metadata module maps returned node or relationship properties into immutable Java records.

```java
@GraphNode("Consultant")
record Consultant(
    @GraphId String id,
    @GraphProperty("display_name") String name,
    int rating) {}

RecordEntityMapper entities =
    new RecordEntityMapper(new ReflectionNodeMetadataRegistry());

Consultant consultant =
    client.execute(query, Neo4jMappers.node("c", Consultant.class, entities)).single();
```

Metadata is validated and cached per record type. Missing primitive properties, duplicate graph
property names, invalid identifiers, unsupported target types, and unsafe numeric narrowing fail
with metadata-specific exceptions. Nested records, `Optional`, typed `List`/`Set` values, enums,
UUIDs, exact numeric conversion, and application `GraphValueConverter` registrations are supported.

## Operations and Observability

`QueryObserver` provides non-sensitive operation, parameter-name, completion, failure, and stream
cancellation callbacks for metrics, tracing, or structured logging. Observer failures are isolated
from database behavior. Applications can expose a custom observer as a Spring bean.

`GraphiteSchemaManager` provides validated, idempotent range-index and uniqueness-constraint
operations. Applications with more elaborate migrations should continue using a dedicated migration
tool and invoke it outside normal request handling.

## Spring Boot

Add `graphite-spring-boot-starter` and configure the connection:

```properties
graphite.uri=bolt://localhost:7687
graphite.username=neo4j
graphite.password=${NEO4J_PASSWORD}
graphite.database=neo4j
graphite.timeout=5s
graphite.fetch-size=1000
graphite.verify-connectivity-on-startup=true
```

The starter supplies the driver, `GraphiteClient`, `GraphiteSpringTemplate`, metadata and converter
registries, schema manager, query observer, and `GraphiteTransactionManager` when the application
has not supplied its own beans. Inject `GraphiteSpringTemplate` for query execution; calls made
inside Spring `@Transactional` methods
participate in the same Neo4j transaction. A read-only transaction uses read routing.

Set `graphite.enabled=false` to disable all Graphite auto-configuration. For advanced driver tuning,
provide a custom Neo4j `Driver` bean.

## Current Scope

The implemented production foundation currently includes:

* immutable reusable path patterns and general ordered read/write clause queries
* multiple and optional match patterns, scoped `WITH`, `UNWIND`, aggregates, and typed projections
* outgoing, incoming, undirected, and variable-length traversals
* grouped `AND`, `OR`, and `NOT` predicates
* metadata-backed typed entities and property references
* parameterized matching, creation, merging, updating, and deletion
* typed relationship writes, conditional merge assignments, and write queries without returns
* searched `CASE`, list/map literals, scoped subqueries, compatible unions, and procedure calls
* optional write returns
* a unified renderer with a public query-renderer extension point
* an explicit raw-Cypher escape hatch
* materialized and closeable streaming Neo4j execution
* query summaries, counters, bookmarks, observations, and typed failure mapping
* managed and explicit transactions, including Spring transaction participation
* cached nested Java-record mapping with generic collections and custom converters
* idempotent index and uniqueness-constraint management
* Spring Boot auto-configuration with overridable beans and external connection settings
* Docker-conditional Neo4j and real Spring transaction integration coverage
* CI enforcement that fails when required container tests cannot start

The compatibility write builders still use fixed operation shapes, while the ordered AST provides
the composable path. Dynamic labels/types, list and pattern comprehensions, procedure `YIELD`
renaming, and vendor-specific clauses still use the raw-Cypher escape hatch. Reactive execution,
generated metamodels, full migration versioning, driver compatibility matrices, and performance
benchmarks remain separate production-hardening tracks.
