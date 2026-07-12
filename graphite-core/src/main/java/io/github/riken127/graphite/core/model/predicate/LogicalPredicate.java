package io.github.riken127.graphite.core.model.predicate;

import java.util.List;
import java.util.Objects;

/** Group of predicates combined by a boolean operator. */
public record LogicalPredicate(LogicalOperator operator, List<Predicate> predicates)
    implements Predicate {

  /** Creates a validated logical predicate. */
  public LogicalPredicate {
    Objects.requireNonNull(operator, "operator must not be null");
    predicates = List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
    if (predicates.size() < 2) {
      throw new IllegalArgumentException("logical predicate requires at least two predicates");
    }
    for (Predicate predicate : predicates) {
      Objects.requireNonNull(predicate, "predicate must not be null");
    }
  }
}
