package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Objects;

/** Renders {@link MatchQuery} AST models into Cypher. */
public final class MatchQueryRenderer {

  /**
   * Renders a MATCH query into Cypher and bound parameters.
   *
   * @param query immutable query model
   * @return rendered query output
   */
  public RenderedQuery render(MatchQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryValidator.validate(query);

    StringBuilder cypher = new StringBuilder();
    ParameterAccumulator parameters = new ParameterAccumulator();

    cypher
        .append("MATCH (")
        .append(query.nodePattern().alias())
        .append(":")
        .append(query.nodePattern().label())
        .append(")");

    if (!query.predicates().isEmpty()) {
      cypher.append(" WHERE ").append(PredicateRenderer.render(query.predicates(), parameters));
    }

    cypher.append(" RETURN ").append(String.join(", ", query.projections()));

    if (!query.sorts().isEmpty()) {
      cypher.append(" ORDER BY ").append(RendererSupport.renderSorts(query.sorts()));
    }

    if (query.skip() != null) {
      cypher.append(" SKIP ").append(query.skip());
    }

    if (query.limit() != null) {
      cypher.append(" LIMIT ").append(query.limit());
    }

    return new RenderedQuery(cypher.toString(), parameters.parameters());
  }
}
