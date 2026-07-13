# Troubleshooting

## Integration tests are skipped

Neo4j integration tests use Testcontainers and skip locally when Docker is unavailable. Confirm the
daemon is reachable with `docker info`. To require Docker and fail instead of skipping, use:

```bash
./mvnw -Dgraphite.requireDocker=true clean verify
```

Override the compatibility image with `-Dgraphite.neo4j.image=neo4j:2026.06.0`.

## Constructor parameters cannot be matched unambiguously

Java reflection does not guarantee field enumeration order. For immutable classes, Graphite matches
constructor parameters by retained parameter name, matching `@GraphId`/`@GraphProperty` annotations,
or a single compatible remaining field.

For models with multiple same-typed fields, either compile the model with `-parameters` or apply the
same mapping annotation to the constructor parameter and its field/accessor:

```java
final class Consultant {
  @GraphId private final String id;
  @GraphProperty("display_name") private final String name;

  Consultant(
      @GraphId String id,
      @GraphProperty("display_name") String name) {
    this.id = id;
    this.name = name;
  }
}
```

Ambiguity is rejected deliberately rather than guessed.

## A generated metamodel is missing

Put `graphite-metamodel-processor` on the compiler's annotation-processor path, not only the normal
dependency classpath. Run `./mvnw clean compile` and inspect `target/generated-sources/annotations`.
The processor reports invalid labels, duplicate graph names, multiple IDs, and ambiguous constructor
mapping as compiler errors.

## Spring Boot does not create Graphite beans

Check that `graphite-spring-boot-starter` is present, `graphite.enabled` is not `false`, and
`graphite.uri` is configured. Auto-configuration backs off when an application supplies its own
`Driver`, `GraphiteClient`, mapping registry, or related bean. Enable Spring Boot's condition report
with `--debug` to see why a condition did not match.

## The Maven wrapper cannot download Maven

The wrapper downloads Maven 3.9.12 from Maven Central and verifies its SHA-256 checksum. Check proxy,
DNS, and TLS interception settings. Configure standard `MAVEN_OPTS` or Maven `settings.xml` proxy
entries; do not remove the checksum to work around a network problem.

## Queries contain unexpected parameters or identifiers

Graphite parameterizes values but renders validated labels, relationship types, property names, and
aliases as identifiers. Never derive identifiers directly from untrusted input. Log rendered Cypher
and parameter names for diagnosis, but redact parameter values and credentials.
