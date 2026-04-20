package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Objects;

/** Renders {@link CreateQuery} AST models into Cypher. */
public final class CreateQueryRenderer {

  /**
   * Renders a CREATE query into Cypher and bound parameters.
   *
   * @param query immutable query model
   * @return rendered query output
   */
  public RenderedQuery render(CreateQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryValidator.validate(query);

    StringBuilder cypher = new StringBuilder();
    ParameterAccumulator parameters = new ParameterAccumulator();

    cypher
        .append("CREATE (")
        .append(query.nodePattern().alias())
        .append(":")
        .append(query.nodePattern().label());

    if (!query.properties().isEmpty()) {
      cypher
          .append(" {")
          .append(PropertyMapRenderer.renderInlineMap(query.properties(), parameters))
          .append("}");
    }

    cypher.append(") RETURN ").append(String.join(", ", query.projections()));

    return new RenderedQuery(cypher.toString(), parameters.parameters());
  }
}
