package io.github.riken127.graphite.neo4j;

import java.util.Set;

/** Non-sensitive query shape supplied to observability hooks. */
public record QueryDescriptor(String operation, Set<String> parameterNames, boolean streaming) {

  /** Creates an immutable query descriptor. */
  public QueryDescriptor {
    if (operation == null || operation.isBlank()) {
      throw new IllegalArgumentException("operation must not be blank");
    }
    operation = operation.trim();
    parameterNames = Set.copyOf(parameterNames);
  }
}
