package io.github.riken127.graphite.core;

import java.util.List;
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
      String alias = AstValidator.requireAlias(projection);
      if (!alias.equals(nodeAlias)) {
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
}
