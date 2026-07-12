package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.PropertyTarget;
import io.github.riken127.graphite.core.model.UpdateQuery;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Renders MATCH-based update queries. */
public final class UpdateQueryRenderer implements QueryRenderer<UpdateQuery> {

  @Override
  public Class<UpdateQuery> queryType() {
    return UpdateQuery.class;
  }

  @Override
  public RenderedQuery render(UpdateQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryValidator.validate(query);
    ParameterAccumulator parameters = new ParameterAccumulator();
    StringBuilder cypher =
        new StringBuilder(
            MatchClauseRenderer.render(query.pathPattern(), query.predicates(), parameters));

    if (!query.assignments().isEmpty()) {
      List<String> assignments = new ArrayList<>();
      for (Map.Entry<PropertyTarget, Object> entry : query.assignments().entrySet()) {
        PropertyTarget target = entry.getKey();
        assignments.add(
            target.alias() + "." + target.property() + " = " + parameters.add(entry.getValue()));
      }
      cypher.append(" SET ").append(String.join(", ", assignments));
    }

    if (!query.removals().isEmpty()) {
      List<String> removals =
          query.removals().stream()
              .map(target -> target.alias() + "." + target.property())
              .toList();
      cypher.append(" REMOVE ").append(String.join(", ", removals));
    }

    if (!query.projections().isEmpty()) {
      cypher.append(" RETURN ").append(String.join(", ", query.projections()));
    }
    return new RenderedQuery(cypher.toString(), parameters.parameters());
  }
}
