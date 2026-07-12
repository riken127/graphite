package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;
import java.util.Optional;

/** Expression selected by a WITH or RETURN clause, optionally under a new alias. */
public record Projection(Expression<?> expression, String alias) {

  /** Creates a validated projection. */
  public Projection {
    Objects.requireNonNull(expression, "expression must not be null");
    alias = alias == null ? null : AstValidator.requireAlias(alias);
  }

  /** Returns a projection without an alias. */
  public static Projection of(Expression<?> expression) {
    return new Projection(expression, null);
  }

  /** Returns a projection with an explicit output alias. */
  public static Projection as(Expression<?> expression, String alias) {
    return new Projection(expression, alias);
  }

  /** Returns the optional output alias. */
  public Optional<String> outputAlias() {
    return Optional.ofNullable(alias);
  }
}
