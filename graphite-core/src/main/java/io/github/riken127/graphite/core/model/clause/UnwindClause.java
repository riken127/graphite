package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Objects;

/** UNWIND expression that introduces one value alias. */
public record UnwindClause(Expression<?> expression, String alias) implements Clause {

  /** Creates a validated UNWIND clause. */
  public UnwindClause {
    Objects.requireNonNull(expression, "expression must not be null");
    alias = AstValidator.requireAlias(alias);
  }
}
