# External consumer tests

These projects deliberately do not inherit from `graphite-parent`. They compile the installed
artifacts as independent Java, annotation-processor, Kotlin, Scala, and Spring Boot consumers.

From the repository root:

```bash
./mvnw -DskipTests install
./mvnw -f consumer-tests/pom.xml verify
```

Keep `graphite.version` aligned with the repository version. Release CI overrides it with the tag's
version when verifying a staged release.
