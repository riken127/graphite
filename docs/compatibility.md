# Compatibility

Graphite targets Java 21 bytecode and tests supported combinations in CI. The matrix below describes
the current `main` branch; a released version's POM and release notes are authoritative.

| Surface | Supported baseline | CI coverage |
| --- | --- | --- |
| Java runtime | JDK 21 and 25 | JDK 21 and 25 |
| Neo4j server | Neo4j 5.26 LTS and 2026.06 | Both lines through Testcontainers |
| Neo4j Java Driver | 6.2 | Compiled and integration tested |
| Spring Framework | 7.0 | Unit and transaction integration tested |
| Spring Boot | 4.0 | Auto-configuration context tested |
| Kotlin | 2.2 | Adapter and external consumer compiled |
| Scala | 3.3 LTS | Adapter and external consumer compiled |
| Maven | Wrapper-provided Maven 3.9.12 | Linux CI |

Graphite does not currently promise Android, GraalVM native image, JPMS module-path, reactive driver,
Neo4j 4.x, Spring Boot 3.x, Kotlin 1.x, or Scala 2.x compatibility.

## Compatibility policy

* The minimum Java language and bytecode level changes only with a documented release.
* Supported Neo4j lines are exercised with real containers, not only mocked driver calls.
* Patch updates within a supported dependency line are expected to work and are handled by
  Dependabot, but a matrix entry is confirmed only after CI passes.
* Pre-1.0 minor releases may contain breaking API changes. They must be called out in the changelog
  and migration notes.

When reporting a compatibility problem, include the exact JDK, Neo4j, driver, framework, and Graphite
versions plus a minimal reproduction.
