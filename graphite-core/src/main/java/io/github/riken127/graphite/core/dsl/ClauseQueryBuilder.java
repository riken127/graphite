package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.clause.CallClause;
import io.github.riken127.graphite.core.model.clause.Clause;
import io.github.riken127.graphite.core.model.clause.CreateClause;
import io.github.riken127.graphite.core.model.clause.DeleteClause;
import io.github.riken127.graphite.core.model.clause.LimitClause;
import io.github.riken127.graphite.core.model.clause.MatchClause;
import io.github.riken127.graphite.core.model.clause.MergeClause;
import io.github.riken127.graphite.core.model.clause.OrderByClause;
import io.github.riken127.graphite.core.model.clause.RemoveClause;
import io.github.riken127.graphite.core.model.clause.ReturnClause;
import io.github.riken127.graphite.core.model.clause.SetAssignment;
import io.github.riken127.graphite.core.model.clause.SetClause;
import io.github.riken127.graphite.core.model.clause.SkipClause;
import io.github.riken127.graphite.core.model.clause.SubqueryClause;
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
  private final List<String> initialScope;

  ClauseQueryBuilder() {
    this(List.of());
  }

  ClauseQueryBuilder(List<String> initialScope) {
    this.initialScope = List.copyOf(initialScope);
  }

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

  /** Adds a CREATE clause containing one or more patterns. */
  public ClauseQueryBuilder create(PathPattern... patterns) {
    clauses.add(new CreateClause(copy(patterns, "patterns")));
    return this;
  }

  /** Adds a MERGE clause without conditional updates. */
  public ClauseQueryBuilder merge(PathPattern pattern) {
    return merge(pattern, List.of(), List.of());
  }

  /** Adds a MERGE clause with ON CREATE and ON MATCH assignments. */
  public ClauseQueryBuilder merge(
      PathPattern pattern,
      List<SetAssignment<?>> onCreateAssignments,
      List<SetAssignment<?>> onMatchAssignments) {
    clauses.add(new MergeClause(pattern, onCreateAssignments, onMatchAssignments));
    return this;
  }

  /** Adds a SET clause. */
  public ClauseQueryBuilder set(SetAssignment<?>... assignments) {
    clauses.add(new SetClause(copy(assignments, "assignments")));
    return this;
  }

  /** Adds a REMOVE clause for one or more properties. */
  public ClauseQueryBuilder remove(Expression<?>... properties) {
    clauses.add(new RemoveClause(copy(properties, "properties")));
    return this;
  }

  /** Adds a DELETE clause. */
  public ClauseQueryBuilder delete(String... aliases) {
    clauses.add(new DeleteClause(copy(aliases, "aliases"), false));
    return this;
  }

  /** Adds a DETACH DELETE clause. */
  public ClauseQueryBuilder detachDelete(String... aliases) {
    clauses.add(new DeleteClause(copy(aliases, "aliases"), true));
    return this;
  }

  /** Calls a procedure conservatively routed as a write. */
  public ClauseQueryBuilder call(
      String procedure, List<Expression<?>> arguments, String... yields) {
    clauses.add(new CallClause(procedure, arguments, copy(yields, "yields"), false));
    return this;
  }

  /** Calls a procedure declared by the caller to be read-only. */
  public ClauseQueryBuilder callReadOnly(
      String procedure, List<Expression<?>> arguments, String... yields) {
    clauses.add(new CallClause(procedure, arguments, copy(yields, "yields"), true));
    return this;
  }

  /** Adds a scoped subquery and imports/exports the named variables. */
  public ClauseQueryBuilder subquery(ClauseQuery query, List<String> imports, String... exports) {
    clauses.add(new SubqueryClause(query, imports, copy(exports, "exports")));
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
    QueryValidator.validate(query, initialScope);
    return query;
  }

  private static <T> List<T> copy(T[] values, String fieldName) {
    Objects.requireNonNull(values, fieldName + " must not be null");
    return List.of(values);
  }
}
