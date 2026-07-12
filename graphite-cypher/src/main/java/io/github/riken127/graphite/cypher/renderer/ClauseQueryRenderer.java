package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.clause.Clause;
import io.github.riken127.graphite.core.model.clause.LimitClause;
import io.github.riken127.graphite.core.model.clause.MatchClause;
import io.github.riken127.graphite.core.model.clause.OrderByClause;
import io.github.riken127.graphite.core.model.clause.ReturnClause;
import io.github.riken127.graphite.core.model.clause.SkipClause;
import io.github.riken127.graphite.core.model.clause.UnwindClause;
import io.github.riken127.graphite.core.model.clause.WhereClause;
import io.github.riken127.graphite.core.model.clause.WithClause;
import io.github.riken127.graphite.core.model.expression.ExpressionSort;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.validation.QueryValidator;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.ArrayList;
import java.util.List;

/** Renders general ordered clause queries. */
public final class ClauseQueryRenderer implements QueryRenderer<ClauseQuery> {

  @Override
  public Class<ClauseQuery> queryType() {
    return ClauseQuery.class;
  }

  @Override
  public RenderedQuery render(ClauseQuery query) {
    QueryValidator.validate(query);
    ParameterAccumulator parameters = new ParameterAccumulator();
    List<String> clauses = new ArrayList<>();
    for (Clause clause : query.clauses()) {
      clauses.add(renderClause(clause, parameters));
    }
    return new RenderedQuery(String.join(" ", clauses), parameters.parameters());
  }

  private static String renderClause(Clause clause, ParameterAccumulator parameters) {
    if (clause instanceof MatchClause match) {
      List<String> patterns = match.patterns().stream().map(PathPatternRenderer::render).toList();
      return (match.optional() ? "OPTIONAL MATCH " : "MATCH ") + String.join(", ", patterns);
    }
    if (clause instanceof WhereClause where) {
      return "WHERE " + PredicateRenderer.render(List.of(where.predicate()), parameters);
    }
    if (clause instanceof WithClause with) {
      return "WITH " + distinct(with.distinct()) + projections(with.projections(), parameters);
    }
    if (clause instanceof UnwindClause unwind) {
      return "UNWIND "
          + ExpressionRenderer.render(unwind.expression(), parameters)
          + " AS "
          + unwind.alias();
    }
    if (clause instanceof ReturnClause returning) {
      return "RETURN "
          + distinct(returning.distinct())
          + projections(returning.projections(), parameters);
    }
    if (clause instanceof OrderByClause orderBy) {
      List<String> sorts = new ArrayList<>();
      for (ExpressionSort sort : orderBy.sorts()) {
        sorts.add(
            ExpressionRenderer.render(sort.expression(), parameters)
                + " "
                + sort.direction().name());
      }
      return "ORDER BY " + String.join(", ", sorts);
    }
    if (clause instanceof SkipClause skip) {
      return "SKIP " + skip.rows();
    }
    if (clause instanceof LimitClause limit) {
      return "LIMIT " + limit.rows();
    }
    throw new IllegalArgumentException("unsupported clause type: " + clause.getClass().getName());
  }

  private static String projections(List<Projection> projections, ParameterAccumulator parameters) {
    return projections.stream()
        .map(projection -> ExpressionRenderer.renderProjection(projection, parameters))
        .reduce((left, right) -> left + ", " + right)
        .orElseThrow();
  }

  private static String distinct(boolean distinct) {
    return distinct ? "DISTINCT " : "";
  }
}
