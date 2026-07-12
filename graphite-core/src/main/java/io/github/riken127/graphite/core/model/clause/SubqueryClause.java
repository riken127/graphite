package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.List;
import java.util.Objects;

/** Scoped CALL subquery with explicit imported and exported variables. */
public record SubqueryClause(ClauseQuery query, List<String> imports, List<String> exports)
    implements Clause {

  /** Creates a validated subquery clause. */
  public SubqueryClause {
    Objects.requireNonNull(query, "query must not be null");
    imports = validatedAliases(imports, "imports");
    exports = validatedAliases(exports, "exports");
  }

  private static List<String> validatedAliases(List<String> aliases, String field) {
    List<String> copied = List.copyOf(Objects.requireNonNull(aliases, field + " must not be null"));
    return copied.stream().map(AstValidator::requireAlias).toList();
  }
}
