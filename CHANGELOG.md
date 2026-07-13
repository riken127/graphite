# Changelog

All notable changes to Graphite are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and releases follow
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

* Immutable query AST, fluent Java DSL, Cypher rendering, metadata mapping, Neo4j execution, Spring
  integration, Spring Boot auto-configuration, and Kotlin and Scala adapters.
* Generated Java metamodels with compile-time validation.
* Checksum-pinned Maven wrapper, source and Javadoc artifacts, coverage gates, dependency
  convergence, and external consumer compilation tests.
* JDK 21/25 and Neo4j 5.26/2026.06 CI compatibility matrix.
* Signed Maven Central release automation and project governance documentation.

### Changed

* Constructor-backed mapping resolves parameters deterministically and rejects ambiguous models
  instead of depending on unspecified reflection field order.

### Security

* GitHub Actions are pinned to immutable commit SHAs and pull requests receive dependency review.

[Unreleased]: https://github.com/riken127/graphite/commits/main
