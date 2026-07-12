package io.github.riken127.graphite.core.model.predicate;

import java.util.Objects;

/** Negation of another predicate. */
public record NotPredicate(Predicate predicate) implements Predicate {

  /** Creates a validated negated predicate. */
  public NotPredicate {
    Objects.requireNonNull(predicate, "predicate must not be null");
  }
}
