package io.github.riken127.graphite.core.validation;

import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.Sort;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    String nodeAlias = query.nodePattern().alias();
    validatePredicates(query.predicates(), nodeAlias);
    validateProjections(query.projections(), nodeAlias);
    validateSorts(query.sorts(), nodeAlias);
  }

  /**
   * Validates a CREATE query model.
   *
   * @param query query to validate
   */
  public static void validate(CreateQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    validateProperties(query.properties(), false, "properties");
    validateProjections(query.projections(), query.nodePattern().alias());
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
    validateProjections(query.projections(), query.nodePattern().alias());
  }

  private static void validatePredicates(List<Predicate> predicates, String nodeAlias) {
    for (Predicate predicate : predicates) {
      Objects.requireNonNull(predicate, "predicate must not be null");
      if (!predicate.alias().equals(nodeAlias)) {
        throw new IllegalArgumentException("predicate alias must match node alias");
      }
    }
  }

  private static void validateProjections(List<String> projections, String nodeAlias) {
    if (projections.isEmpty()) {
      throw new IllegalArgumentException("projections must not be empty");
    }

    for (String projection : projections) {
      String validatedProjection = AstValidator.requireProjection(projection);
      String projectionAlias = AstValidator.projectionAlias(validatedProjection);
      if (!projectionAlias.equals(nodeAlias)) {
        throw new IllegalArgumentException("projection alias must match node alias");
      }
    }
  }

  private static void validateSorts(List<Sort> sorts, String nodeAlias) {
    for (Sort sort : sorts) {
      Objects.requireNonNull(sort, "sort must not be null");
      if (!sort.alias().equals(nodeAlias)) {
        throw new IllegalArgumentException("sort alias must match node alias");
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
