package io.github.riken127.graphite.core.model.clause;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.List;
import java.util.Objects;

/** DELETE or DETACH DELETE clause. */
public record DeleteClause(List<String> aliases, boolean detach) implements Clause {

  /** Creates a validated delete clause. */
  public DeleteClause {
    aliases = List.copyOf(Objects.requireNonNull(aliases, "aliases must not be null"));
    if (aliases.isEmpty()) {
      throw new IllegalArgumentException("aliases must not be empty");
    }
    aliases = aliases.stream().map(AstValidator::requireAlias).toList();
  }
}
