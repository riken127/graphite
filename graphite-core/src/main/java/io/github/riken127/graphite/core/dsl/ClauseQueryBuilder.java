package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.clause.Clause;
import io.github.riken127.graphite.core.model.clause.LimitClause;
import io.github.riken127.graphite.core.model.clause.MatchClause;
import io.github.riken127.graphite.core.model.clause.OrderByClause;
import io.github.riken127.graphite.core.model.clause.ReturnClause;
import io.github.riken127.graphite.core.model.clause.SkipClause;
import io.github.riken127.graphite.core.model.clause.UnwindClause;
import io.github.riken127.graphite.core.model.clause.WhereClause;
import io.github.riken127.graphite.core.model.clause.WithClause;
import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.model.expression.ExpressionSort;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.validation.QueryValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Fluent builder for a general ordered clause query. */
public final class ClauseQueryBuilder {

  private final List<Clause> clauses = new ArrayList<>();

  ClauseQueryBuilder() {}

  /** Adds a MATCH clause containing one or more patterns. */
  public ClauseQueryBuilder match(PathPattern... patterns) {
    clauses.add(new MatchClause(copy(patterns, "patterns"), false));
    return this;
  }

  /** Adds an OPTIONAL MATCH clause containing one or more patterns. */
  public ClauseQueryBuilder optionalMatch(PathPattern... patterns) {
    clauses.add(new MatchClause(copy(patterns, "patterns"), true));
    return this;
  }

  /** Adds a WHERE clause. */
  public ClauseQueryBuilder where(Predicate predicate) {
    clauses.add(new WhereClause(predicate));
    return this;
  }

  /** Adds a WITH scope boundary. */
  public ClauseQueryBuilder with(Projection... projections) {
    return with(false, projections);
  }

  /** Adds a WITH scope boundary with optional duplicate elimination. */
  public ClauseQueryBuilder with(boolean distinct, Projection... projections) {
    clauses.add(new WithClause(copy(projections, "projections"), distinct));
    return this;
  }

  /** Adds an UNWIND clause. */
  public ClauseQueryBuilder unwind(Expression<?> expression, String alias) {
    clauses.add(new UnwindClause(expression, alias));
    return this;
  }

  /** Adds a RETURN clause. */
  public ClauseQueryBuilder returning(Projection... projections) {
    return returning(false, projections);
  }

  /** Adds a RETURN clause with optional duplicate elimination. */
  public ClauseQueryBuilder returning(boolean distinct, Projection... projections) {
    clauses.add(new ReturnClause(copy(projections, "projections"), distinct));
    return this;
  }

  /** Adds expression-based ordering. */
  public ClauseQueryBuilder orderBy(ExpressionSort... sorts) {
    clauses.add(new OrderByClause(copy(sorts, "sorts")));
    return this;
  }

  /** Adds a SKIP clause. */
  public ClauseQueryBuilder skip(int rows) {
    clauses.add(new SkipClause(rows));
    return this;
  }

  /** Adds a LIMIT clause. */
  public ClauseQueryBuilder limit(int rows) {
    clauses.add(new LimitClause(rows));
    return this;
  }

  /** Builds and validates an immutable clause query. */
  public ClauseQuery build() {
    ClauseQuery query = new ClauseQuery(clauses);
    QueryValidator.validate(query);
    return query;
  }

  private static <T> List<T> copy(T[] values, String fieldName) {
    Objects.requireNonNull(values, fieldName + " must not be null");
    return List.of(values);
  }
}
