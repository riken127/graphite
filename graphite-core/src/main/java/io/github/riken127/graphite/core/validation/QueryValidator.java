package io.github.riken127.graphite.core.validation;

import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.PropertyTarget;
import io.github.riken127.graphite.core.model.Sort;
import io.github.riken127.graphite.core.model.UpdateQuery;
import io.github.riken127.graphite.core.model.predicate.ComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import io.github.riken127.graphite.core.model.predicate.LogicalPredicate;
import io.github.riken127.graphite.core.model.predicate.NotPredicate;
import io.github.riken127.graphite.core.model.predicate.NullPredicate;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.model.predicate.TextPredicate;
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

    String alias = predicateAlias(predicate);
    if (!aliases.contains(alias)) {
      throw new IllegalArgumentException("predicate alias must exist in matched pattern: " + alias);
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
