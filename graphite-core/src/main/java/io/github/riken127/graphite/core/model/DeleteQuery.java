package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.List;
import java.util.Objects;

/** Immutable AST model for a MATCH-based deletion. */
public record DeleteQuery(
    PathPattern pathPattern, List<Predicate> predicates, List<String> aliases, boolean detach)
    implements Query {

  /** Creates a validated delete query model. */
  public DeleteQuery {
    Objects.requireNonNull(pathPattern, "pathPattern must not be null");
    predicates = List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    aliases =
        Objects.requireNonNull(aliases, "aliases must not be null").stream()
            .map(AstValidator::requireAlias)
            .toList();
    if (aliases.isEmpty()) {
      throw new IllegalArgumentException("aliases must not be empty");
    }
  }
}
