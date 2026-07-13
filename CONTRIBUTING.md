# Contributing to Graphite

Thank you for helping improve Graphite. Small, focused changes with clear tests are easiest to
review and maintain.

## Development setup

Requirements:

* JDK 21 or 25
* Docker for Neo4j integration tests

Use the checksum-pinned Maven wrapper; a separate Maven installation is not required.

```bash
./mvnw spotless:apply
./mvnw clean verify
```

Without Docker, the Neo4j integration tests are skipped. To make missing Docker a failure, matching
CI behavior, run:

```bash
./mvnw -Dgraphite.requireDocker=true clean verify
```

To verify the public artifacts as independent consumers:

```bash
./mvnw -DskipTests -Djacoco.skip=true install
./mvnw -f consumer-tests/pom.xml verify
```

## Change guidelines

* Open an issue before a large API or architecture change.
* Preserve the boundaries in [ARCHITECTURE.md](ARCHITECTURE.md).
* Add focused unit or integration tests for behavior changes.
* Update README, compatibility, troubleshooting, or migration guidance when user-facing behavior
  changes.
* Keep public APIs documented and avoid exposing framework types from framework-independent modules.
* Use conventional commit subjects such as `feat:`, `fix:`, `docs:`, `test:`, `build:`, and `ci:`.

Pull requests should explain the problem, the chosen design, compatibility impact, and verification
performed. By participating, you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
