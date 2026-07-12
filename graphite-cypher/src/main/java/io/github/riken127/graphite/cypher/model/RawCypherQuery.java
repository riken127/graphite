package io.github.riken127.graphite.cypher.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Explicit escape hatch for trusted Cypher text with separately bound parameters. */
public record RawCypherQuery(String cypher, Map<String, Object> parameters) {

  /** Creates a validated raw query. */
  public RawCypherQuery {
    if (cypher == null || cypher.isBlank()) {
      throw new IllegalArgumentException("cypher must not be blank");
    }
    parameters =
        Collections.unmodifiableMap(
            new LinkedHashMap<>(Objects.requireNonNull(parameters, "parameters must not be null")));
  }
}
