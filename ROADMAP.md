# Graphite Roadmap

## Vision

Build the most natural way to use graph databases from Java.

## Phase 0 — Foundation

Status: Complete

Goals:

* Define project philosophy
* Establish architecture boundaries
* Create multi-module structure
* Configure formatting and CI
* Build graphite-core skeleton

Deliverables:

* Philosophy docs
* Architecture docs
* Parent build
* Core packages

## Phase 1 — Core Query Model

Status: Complete

Goals:

* Immutable AST model
* Node patterns
* Relationship patterns
* Predicates
* Sorting
* Pagination
* Query validation

Deliverables:

* MatchQuery
* CreateQuery
* MergeQuery
* Expression system
* Validation engine

## Phase 2 — Fluent DSL

Status: Complete

Goals:

* Human-readable Java query API
* Composable builders
* Reusable filters
* Clean projections

Deliverables:

* match(...)
* where(...)
* select(...)
* orderBy(...)
* limit(...)

## Phase 3 — Cypher Renderer

Status: Complete

Goals:

* Convert AST into valid Cypher
* Parameter binding
* Stable aliasing
* Test coverage

Deliverables:

* MATCH renderer
* MERGE renderer
* CREATE renderer
* Predicate renderer

## Phase 4 — Spring Integration

Status: Complete for synchronous execution

Goals:

* Execution support in Spring apps
* Transaction participation
* Simple setup experience

Deliverables:

* GraphiteClient and GraphiteSpringTemplate
* Spring transaction manager
* Boot auto-configuration starter

## Phase 5 — Production Readiness

Status: In progress

Goals:

* Documentation
* Benchmarks
* Error ergonomics
* Migration guides
* Real examples

Completed foundations:

* Stable runtime result and exception contracts
* Query options, causal bookmarks, and transaction APIs
* Cached Java-record and immutable-object mapping
* Neo4j Testcontainers integration suite
* Compatibility, troubleshooting, performance-policy, versioning, and release documentation
* Checksum-pinned Maven builds with source, Javadoc, coverage, and dependency-convergence gates
* JDK and Neo4j compatibility CI plus external Java/Kotlin/Scala/Spring Boot consumer builds
* Signed Maven Central and GitHub release automation

Remaining work:

* Expression and pattern comprehensions plus vendor-specific clauses
* Reproducible benchmark baselines
* Versioned migration tooling
* KSP and native Scala metamodel generation

Recently completed:

* Typed expression and ordered read-clause AST
* Metadata-backed typed entity references
* Closeable streaming execution
* Nested/generic record mapping and converter extensions
* Query observation and schema-management foundations
* CI-enforced container and real Spring transaction tests
* Reusable test helpers and a runnable runtime example
* General write clauses, searched CASE/list/map expressions, scoped subqueries, unions, and calls
* Optional Kotlin and Scala 3 DSL adapters with native typed entity references
* Constructor-backed Java/Kotlin/Scala object mapping with custom factories
* Generated Java metamodels with compile-time mapping validation

Delivered:

* Runnable examples and independent consumer projects
* Performance policy and benchmark acceptance criteria
* Compatibility and troubleshooting documentation

## Phase 6 — Advanced Features

Status: Pending

Goals:

* KSP and native Scala metamodel derivation
* Lambda property references
* Reactive support
* Query analysis
* Metrics hooks

## Non-Priority Items

These should not distract early momentum:

* Graph UI tooling
* Full ORM behaviour
* Code generation heavy systems
* Multi-backend ambitions too early

## Success Metrics

Graphite succeeds when developers say:

* "I no longer write raw Cypher for common tasks."
* "My graph queries are readable."
* "Refactors are safer."
* "Spring + Neo4j finally feels coherent."

## Final Principle

Ship small, useful increments.
Do not chase grand architecture without user value.
