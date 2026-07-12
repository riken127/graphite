package io.github.riken127.graphite.core.model;

import java.util.List;
import java.util.Objects;

/** Query composed from compatible clause-query branches joined with UNION. */
public record UnionQuery(List<ClauseQuery> branches, boolean all) implements Query {

  /** Creates a validated UNION query container. */
  public UnionQuery {
    branches = List.copyOf(Objects.requireNonNull(branches, "branches must not be null"));
    if (branches.size() < 2) {
      throw new IllegalArgumentException("UNION requires at least two branches");
    }
    branches.forEach(branch -> Objects.requireNonNull(branch, "branch must not be null"));
  }
}
