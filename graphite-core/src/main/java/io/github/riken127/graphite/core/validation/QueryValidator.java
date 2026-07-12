package io.github.riken127.graphite.core.validation;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.PropertyTarget;
import io.github.riken127.graphite.core.model.Sort;
import io.github.riken127.graphite.core.model.UpdateQuery;
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
import io.github.riken127.graphite.core.model.expression.FunctionExpression;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.model.expression.PropertyExpression;
import io.github.riken127.graphite.core.model.expression.VariableExpression;
import io.github.riken127.graphite.core.model.predicate.ComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionInPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionNullPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionTextPredicate;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import io.github.riken127.graphite.core.model.predicate.LogicalPredicate;
import io.github.riken127.graphite.core.model.predicate.NotPredicate;
import io.github.riken127.graphite.core.model.predicate.NullPredicate;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.model.predicate.TextPredicate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Validates immutable query models before rendering or execution. */
public final class QueryValidator {

  private QueryValidator() {}

  /**
   * Validates a MATCH query model.
   *
   * @param query query to validate
   */
  public static void validate(MatchQuery query) {
    Objects.requireNonNull(query, "query must not be null");

    Set<String> aliases = query.pathPattern().aliases();
    validatePredicates(query.predicates(), aliases);
    validateRequiredProjections(query.projections(), aliases);
    validateSorts(query.sorts(), aliases);
  }

  /**
   * Validates a CREATE query model.
   *
   * @param query query to validate
   */
  public static void validate(CreateQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    validateProperties(query.properties(), false, "properties");
    validateOptionalProjections(query.projections(), Set.of(query.nodePattern().alias()));
  }

  /**
   * Validates a MERGE query model.
   *
   * @param query query to validate
   */
  public static void validate(MergeQuery query) {
    Objects.requireNonNull(query, "query must not be null");

    validateProperties(query.identityProperties(), true, "identityProperties");
    validateProperties(query.onCreateProperties(), false, "onCreateProperties");
    validateProperties(query.onMatchProperties(), false, "onMatchProperties");
    validateOptionalProjections(query.projections(), Set.of(query.nodePattern().alias()));
  }

  /** Validates an update query model. */
  public static void validate(UpdateQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    Set<String> aliases = query.pathPattern().aliases();
    validatePredicates(query.predicates(), aliases);
    validateTargets(query.assignments().keySet(), aliases);
    validateTargets(query.removals(), aliases);
    validateOptionalProjections(query.projections(), aliases);
    if (query.assignments().isEmpty() && query.removals().isEmpty()) {
      throw new IllegalArgumentException("update must assign or remove at least one property");
    }
  }

  /** Validates a delete query model. */
  public static void validate(DeleteQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    Set<String> aliases = query.pathPattern().aliases();
    validatePredicates(query.predicates(), aliases);
    for (String alias : query.aliases()) {
      if (!aliases.contains(alias)) {
        throw new IllegalArgumentException("delete alias must exist in matched pattern: " + alias);
      }
      if (query.detach() && !query.pathPattern().nodeAliases().contains(alias)) {
        throw new IllegalArgumentException("DETACH DELETE requires node aliases: " + alias);
      }
    }
  }

  /** Validates a general ordered clause query. */
  public static void validate(ClauseQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    Set<String> scope = new LinkedHashSet<>();
    boolean returned = false;
    boolean projected = false;

    for (Clause clause : query.clauses()) {
      if (returned
          && !(clause instanceof OrderByClause
              || clause instanceof SkipClause
              || clause instanceof LimitClause)) {
        throw new IllegalArgumentException("RETURN must be the final value-producing clause");
      }
      if (clause instanceof MatchClause matchClause) {
        for (var pattern : matchClause.patterns()) {
          scope.addAll(pattern.aliases());
        }
        projected = false;
      } else if (clause instanceof WhereClause whereClause) {
        requireScope(scope, "WHERE");
        validatePredicate(whereClause.predicate(), scope);
      } else if (clause instanceof UnwindClause unwindClause) {
        validateExpression(unwindClause.expression(), scope);
        scope.add(unwindClause.alias());
        projected = false;
      } else if (clause instanceof WithClause withClause) {
        requireScope(scope, "WITH");
        validateProjections(withClause.projections(), scope);
        scope = projectionScope(withClause.projections());
        projected = true;
      } else if (clause instanceof ReturnClause returnClause) {
        requireScope(scope, "RETURN");
        validateProjections(returnClause.projections(), scope);
        for (Projection projection : returnClause.projections()) {
          projection.outputAlias().ifPresent(scope::add);
        }
        returned = true;
        projected = true;
      } else if (clause instanceof OrderByClause orderByClause) {
        if (!projected) {
          throw new IllegalArgumentException("ORDER BY must follow WITH or RETURN");
        }
        for (var sort : orderByClause.sorts()) {
          validateExpression(sort.expression(), scope);
        }
      } else if (clause instanceof SkipClause || clause instanceof LimitClause) {
        if (!projected) {
          throw new IllegalArgumentException("SKIP and LIMIT must follow WITH or RETURN");
        }
      }
    }

    if (!returned) {
      throw new IllegalArgumentException("clause query must contain RETURN");
    }
  }

  private static void validatePredicates(List<Predicate> predicates, Set<String> aliases) {
    for (Predicate predicate : predicates) {
      Objects.requireNonNull(predicate, "predicate must not be null");
      validatePredicate(predicate, aliases);
    }
  }

  private static void validatePredicate(Predicate predicate, Set<String> aliases) {
    if (predicate instanceof LogicalPredicate logicalPredicate) {
      validatePredicates(logicalPredicate.predicates(), aliases);
      return;
    }
    if (predicate instanceof NotPredicate notPredicate) {
      validatePredicate(notPredicate.predicate(), aliases);
      return;
    }

    if (predicate instanceof ExpressionComparisonPredicate comparison) {
      validateExpression(comparison.left(), aliases);
      validateExpression(comparison.right(), aliases);
      return;
    }
    if (predicate instanceof ExpressionInPredicate inPredicate) {
      validateExpression(inPredicate.value(), aliases);
      validateExpression(inPredicate.collection(), aliases);
      return;
    }
    if (predicate instanceof ExpressionNullPredicate nullPredicate) {
      validateExpression(nullPredicate.expression(), aliases);
      return;
    }
    if (predicate instanceof ExpressionTextPredicate textPredicate) {
      validateExpression(textPredicate.value(), aliases);
      validateExpression(textPredicate.expected(), aliases);
      return;
    }

    String alias = predicateAlias(predicate);
    if (!aliases.contains(alias)) {
      throw new IllegalArgumentException("predicate alias must exist in matched pattern: " + alias);
    }
  }

  private static void validateProjections(List<Projection> projections, Set<String> aliases) {
    for (Projection projection : projections) {
      validateExpression(projection.expression(), aliases);
    }
  }

  private static void validateExpression(Expression<?> expression, Set<String> aliases) {
    if (expression instanceof VariableExpression<?> variable) {
      requireAlias(variable.alias(), aliases, "expression");
    } else if (expression instanceof PropertyExpression<?> property) {
      requireAlias(property.alias(), aliases, "expression");
    } else if (expression
        instanceof io.github.riken127.graphite.core.dsl.TypedPropertyRef<?> typed) {
      requireAlias(typed.alias(), aliases, "expression");
    } else if (expression instanceof FunctionExpression<?> function) {
      function.arguments().forEach(argument -> validateExpression(argument, aliases));
    }
  }

  private static Set<String> projectionScope(List<Projection> projections) {
    Set<String> result = new LinkedHashSet<>();
    for (Projection projection : projections) {
      if (projection.alias() != null) {
        result.add(projection.alias());
      } else if (projection.expression() instanceof VariableExpression<?> variable) {
        result.add(variable.alias());
      }
    }
    if (result.isEmpty()) {
      throw new IllegalArgumentException(
          "WITH expressions must preserve a variable or declare an output alias");
    }
    return result;
  }

  private static void requireScope(Set<String> scope, String clause) {
    if (scope.isEmpty()) {
      throw new IllegalArgumentException(clause + " requires variables in scope");
    }
  }

  private static void requireAlias(String alias, Set<String> aliases, String source) {
    if (!aliases.contains(alias)) {
      throw new IllegalArgumentException(source + " alias is not in scope: " + alias);
    }
  }

  private static String predicateAlias(Predicate predicate) {
    if (predicate instanceof ComparisonPredicate comparisonPredicate) {
      return comparisonPredicate.alias();
    }
    if (predicate instanceof InPredicate inPredicate) {
      return inPredicate.alias();
    }
    if (predicate instanceof NullPredicate nullPredicate) {
      return nullPredicate.alias();
    }
    if (predicate instanceof TextPredicate textPredicate) {
      return textPredicate.alias();
    }
    throw new IllegalArgumentException(
        "unsupported predicate type: " + predicate.getClass().getName());
  }

  private static void validateRequiredProjections(List<String> projections, Set<String> aliases) {
    if (projections.isEmpty()) {
      throw new IllegalArgumentException("projections must not be empty");
    }

    validateOptionalProjections(projections, aliases);
  }

  private static void validateOptionalProjections(List<String> projections, Set<String> aliases) {
    for (String projection : projections) {
      String validatedProjection = AstValidator.requireProjection(projection);
      String projectionAlias = AstValidator.projectionAlias(validatedProjection);
      if (!aliases.contains(projectionAlias)) {
        throw new IllegalArgumentException(
            "projection alias must exist in matched pattern: " + projectionAlias);
      }
    }
  }

  private static void validateSorts(List<Sort> sorts, Set<String> aliases) {
    for (Sort sort : sorts) {
      Objects.requireNonNull(sort, "sort must not be null");
      if (!aliases.contains(sort.alias())) {
        throw new IllegalArgumentException(
            "sort alias must exist in matched pattern: " + sort.alias());
      }
    }
  }

  private static void validateTargets(Iterable<PropertyTarget> targets, Set<String> aliases) {
    for (PropertyTarget target : targets) {
      Objects.requireNonNull(target, "property target must not be null");
      if (!aliases.contains(target.alias())) {
        throw new IllegalArgumentException(
            "property target alias must exist in matched pattern: " + target.alias());
      }
    }
  }

  private static void validateProperties(
      Map<String, Object> properties, boolean requireAtLeastOne, String fieldName) {
    Objects.requireNonNull(properties, fieldName + " must not be null");
    if (requireAtLeastOne && properties.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be empty");
    }

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      AstValidator.requireProperty(entry.getKey());
      Objects.requireNonNull(entry.getValue(), "property value must not be null");
    }
  }
}
