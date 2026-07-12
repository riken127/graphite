package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Procedure invocation with explicit read-only intent and yielded variables. */
public record CallClause(
    String procedure, List<Expression<?>> arguments, List<String> yields, boolean readOnly)
    implements Clause {

  private static final Pattern PROCEDURE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");

  /** Creates a validated procedure call. */
  public CallClause {
    if (procedure == null || !PROCEDURE_NAME.matcher(procedure).matches()) {
      throw new IllegalArgumentException("invalid procedure name: " + procedure);
    }
    arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    arguments.forEach(value -> Objects.requireNonNull(value, "argument must not be null"));
    yields = List.copyOf(Objects.requireNonNull(yields, "yields must not be null"));
    yields = yields.stream().map(AstValidator::requireAlias).toList();
  }
}
