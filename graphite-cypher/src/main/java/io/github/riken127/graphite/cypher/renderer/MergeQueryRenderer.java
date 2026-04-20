package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Objects;

/** Renders {@link MergeQuery} AST models into Cypher. */
public final class MergeQueryRenderer {

  /**
   * Renders a MERGE query into Cypher and bound parameters.
   *
   * @param query immutable query model
   * @return rendered query output
   */
  public RenderedQuery render(MergeQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryValidator.validate(query);

    StringBuilder cypher = new StringBuilder();
    ParameterAccumulator parameters = new ParameterAccumulator();

    cypher
        .append("MERGE (")
        .append(query.nodePattern().alias())
        .append(":")
        .append(query.nodePattern().label())
        .append(" {")
        .append(PropertyMapRenderer.renderInlineMap(query.identityProperties(), parameters))
        .append("})");

    if (!query.onCreateProperties().isEmpty()) {
      cypher
          .append(" ON CREATE SET ")
          .append(
              PropertyMapRenderer.renderSetStatements(
                  query.nodePattern().alias(), query.onCreateProperties(), parameters));
    }

    if (!query.onMatchProperties().isEmpty()) {
      cypher
          .append(" ON MATCH SET ")
          .append(
              PropertyMapRenderer.renderSetStatements(
                  query.nodePattern().alias(), query.onMatchProperties(), parameters));
    }

    cypher.append(" RETURN ").append(String.join(", ", query.projections()));

    return new RenderedQuery(cypher.toString(), parameters.parameters());
  }
}
