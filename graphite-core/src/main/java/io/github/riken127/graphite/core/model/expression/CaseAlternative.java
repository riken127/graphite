package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.Objects;

/** One WHEN predicate and THEN value in a searched CASE expression. */
public record CaseAlternative<T>(Predicate when, Expression<? extends T> then) {

  /** Creates a validated CASE alternative. */
  public CaseAlternative {
    Objects.requireNonNull(when, "when must not be null");
    Objects.requireNonNull(then, "then must not be null");
  }
}
