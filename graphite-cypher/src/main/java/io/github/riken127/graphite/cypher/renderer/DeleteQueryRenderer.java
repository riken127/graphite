package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Objects;

/** Renders MATCH-based delete queries. */
public final class DeleteQueryRenderer implements QueryRenderer<DeleteQuery> {

  @Override
  public Class<DeleteQuery> queryType() {
    return DeleteQuery.class;
  }

  @Override
  public RenderedQuery render(DeleteQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryValidator.validate(query);
    ParameterAccumulator parameters = new ParameterAccumulator();
    String cypher =
        MatchClauseRenderer.render(query.pathPattern(), query.predicates(), parameters)
            + (query.detach() ? " DETACH DELETE " : " DELETE ")
            + String.join(", ", query.aliases());
    return new RenderedQuery(cypher, parameters.parameters());
  }
}
