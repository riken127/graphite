# Graphite Roadmap

## Vision

Build the most natural way to use graph databases from Java.

## Phase 0 — Foundation

Status: Current

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

Goals:

* Execution support in Spring apps
* Transaction participation
* Simple setup experience

Deliverables:

* GraphiteTemplate
* Spring adapter
* Boot starter

## Phase 5 — Production Readiness

Goals:

* Documentation
* Benchmarks
* Error ergonomics
* Migration guides
* Real examples

Deliverables:

* Samples repo
* Performance docs
* Troubleshooting docs

## Phase 6 — Advanced Features

Goals:

* Generated metamodels
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
