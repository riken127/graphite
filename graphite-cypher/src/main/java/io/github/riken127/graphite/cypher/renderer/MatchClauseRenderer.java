package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.List;

final class MatchClauseRenderer {

  private MatchClauseRenderer() {}

  static String render(
      PathPattern pathPattern, List<Predicate> predicates, ParameterAccumulator parameters) {
    StringBuilder cypher =
        new StringBuilder("MATCH ").append(PathPatternRenderer.render(pathPattern));
    if (!predicates.isEmpty()) {
      cypher.append(" WHERE ").append(PredicateRenderer.render(predicates, parameters));
    }
    return cypher.toString();
  }
}
