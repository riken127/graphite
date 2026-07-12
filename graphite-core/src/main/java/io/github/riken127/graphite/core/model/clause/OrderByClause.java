package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.ExpressionSort;
import java.util.List;
import java.util.Objects;

/** ORDER BY clause for arbitrary typed expressions. */
public record OrderByClause(List<ExpressionSort> sorts) implements Clause {

  /** Creates a validated ORDER BY clause. */
  public OrderByClause {
    sorts = List.copyOf(Objects.requireNonNull(sorts, "sorts must not be null"));
    if (sorts.isEmpty()) {
      throw new IllegalArgumentException("sorts must not be empty");
    }
    sorts.forEach(sort -> Objects.requireNonNull(sort, "sort must not be null"));
  }
}
