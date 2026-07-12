package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.validation.QueryValidator;
import java.util.List;
import java.util.Objects;

/** Fluent builder for a MATCH-based deletion. */
public final class DeleteBuilder {

  private final PathPattern pathPattern;
  private final List<Predicate> predicates;
  private final List<String> aliases;
  private final boolean detach;

  DeleteBuilder(
      PathPattern pathPattern, List<Predicate> predicates, List<String> aliases, boolean detach) {
    this.pathPattern = Objects.requireNonNull(pathPattern, "pathPattern must not be null");
    this.predicates =
        List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    this.aliases = List.copyOf(Objects.requireNonNull(aliases, "aliases must not be null"));
    this.detach = detach;
  }

  /** Builds an immutable delete query. */
  public DeleteQuery build() {
    DeleteQuery query = new DeleteQuery(pathPattern, predicates, aliases, detach);
    QueryValidator.validate(query);
    return query;
  }
}
