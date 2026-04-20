package io.github.riken127.graphite.cypher.model;

import java.util.Map;
import java.util.Objects;

/** Immutable output from Cypher rendering. */
public record RenderedQuery(String cypher, Map<String, Object> parameters) {

  /**
   * Creates a rendered query result.
   *
   * @param cypher rendered query text
   * @param parameters bound query parameters
   */
  public RenderedQuery {
    if (cypher == null || cypher.isBlank()) {
      throw new IllegalArgumentException("cypher must not be blank");
    }
    parameters = Map.copyOf(Objects.requireNonNull(parameters, "parameters must not be null"));
  }
}
