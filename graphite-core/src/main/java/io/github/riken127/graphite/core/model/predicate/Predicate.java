package io.github.riken127.graphite.core.model.predicate;

/** Base contract for query predicates. */
public sealed interface Predicate
    permits ComparisonPredicate,
        InPredicate,
        LogicalPredicate,
        NotPredicate,
        NullPredicate,
        TextPredicate {

  /** Combines this predicate with another using AND. */
  default Predicate and(Predicate other) {
    return new LogicalPredicate(LogicalOperator.AND, java.util.List.of(this, other));
  }

  /** Combines this predicate with another using OR. */
  default Predicate or(Predicate other) {
    return new LogicalPredicate(LogicalOperator.OR, java.util.List.of(this, other));
  }

  /** Negates this predicate. */
  default Predicate not() {
    return new NotPredicate(this);
  }
}
