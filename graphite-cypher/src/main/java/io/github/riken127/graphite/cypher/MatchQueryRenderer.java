package io.github.riken127.graphite.cypher;

import io.github.riken127.graphite.core.EqualsPredicate;
import io.github.riken127.graphite.core.MatchQuery;
import io.github.riken127.graphite.core.Predicate;
import io.github.riken127.graphite.core.QueryValidator;
import io.github.riken127.graphite.core.Sort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    Map<String, Object> parameters = new LinkedHashMap<>();

    cypher
        .append("MATCH (")
        .append(query.nodePattern().alias())
        .append(":")
        .append(query.nodePattern().label())
        .append(")");

    if (!query.predicates().isEmpty()) {
      cypher.append(" WHERE ").append(renderPredicates(query.predicates(), parameters));
    }

    cypher.append(" RETURN ").append(String.join(", ", query.projections()));

    if (!query.sorts().isEmpty()) {
      cypher.append(" ORDER BY ").append(renderSorts(query.sorts()));
    }

    if (query.limit() != null) {
      cypher.append(" LIMIT ").append(query.limit());
    }

    return new RenderedQuery(cypher.toString(), parameters);
  }

  private static String renderPredicates(
      List<Predicate> predicates, Map<String, Object> parameters) {
    List<String> parts = new ArrayList<>();

    int parameterIndex = 0;
    for (Predicate predicate : predicates) {
      if (predicate instanceof EqualsPredicate equalsPredicate) {
        String parameterName = "p" + parameterIndex++;
        parameters.put(parameterName, equalsPredicate.value());
        parts.add(
            equalsPredicate.alias() + "." + equalsPredicate.property() + " = $" + parameterName);
      } else {
        throw new IllegalArgumentException(
            "unsupported predicate type: " + predicate.getClass().getSimpleName());
      }
    }

    return String.join(" AND ", parts);
  }

  private static String renderSorts(List<Sort> sorts) {
    List<String> parts = new ArrayList<>();
    for (Sort sort : sorts) {
      parts.add(sort.alias() + "." + sort.property() + " " + sort.direction().name());
    }
    return String.join(", ", parts);
  }
}
