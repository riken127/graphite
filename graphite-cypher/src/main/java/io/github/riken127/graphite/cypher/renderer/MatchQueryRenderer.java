package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Objects;

/** Renders {@link MatchQuery} AST models into Cypher. */
public final class MatchQueryRenderer implements QueryRenderer<MatchQuery> {

  @Override
  public Class<MatchQuery> queryType() {
    return MatchQuery.class;
  }

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

    cypher.append(MatchClauseRenderer.render(query.pathPattern(), query.predicates(), parameters));

    cypher.append(" RETURN ").append(String.join(", ", query.projections()));

    if (!query.sorts().isEmpty()) {
      cypher.append(" ORDER BY ").append(RendererSupport.renderSorts(query.sorts()));
    }

    if (query.skip() != null) {
      cypher.append(" SKIP ").append(parameters.add(query.skip()));
    }

    if (query.limit() != null) {
      cypher.append(" LIMIT ").append(parameters.add(query.limit()));
    }

    return new RenderedQuery(cypher.toString(), parameters.parameters());
  }
}
