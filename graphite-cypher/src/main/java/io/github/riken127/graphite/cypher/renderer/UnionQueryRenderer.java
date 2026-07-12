package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.UnionQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;

/** Renders compatible clause-query branches joined by UNION. */
public final class UnionQueryRenderer implements QueryRenderer<UnionQuery> {

  @Override
  public Class<UnionQuery> queryType() {
    return UnionQuery.class;
  }

  @Override
  public RenderedQuery render(UnionQuery query) {
    QueryValidator.validate(query);
    ParameterAccumulator parameters = new ParameterAccumulator();
    String separator = query.all() ? " UNION ALL " : " UNION ";
    String cypher =
        query.branches().stream()
            .map(branch -> ClauseQueryRenderer.renderClauses(branch, parameters))
            .reduce((left, right) -> left + separator + right)
            .orElseThrow();
    return new RenderedQuery(cypher, parameters.parameters());
  }
}
