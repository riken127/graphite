package io.github.riken127.graphite.cypher;

import io.github.riken127.graphite.core.MergeQuery;
import io.github.riken127.graphite.core.QueryValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    Map<String, Object> parameters = new LinkedHashMap<>();

    cypher
        .append("MERGE (")
        .append(query.nodePattern().alias())
        .append(":")
        .append(query.nodePattern().label())
        .append(" {")
        .append(renderProperties(query.properties(), parameters))
        .append("}) RETURN ")
        .append(String.join(", ", query.projections()));

    return new RenderedQuery(cypher.toString(), parameters);
  }

  private static String renderProperties(
      Map<String, Object> properties, Map<String, Object> parameters) {
    List<String> parts = new ArrayList<>();
    int parameterIndex = 0;

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String parameterName = "p" + parameterIndex++;
      parameters.put(parameterName, entry.getValue());
      parts.add(entry.getKey() + ": $" + parameterName);
    }

    return String.join(", ", parts);
  }
}
