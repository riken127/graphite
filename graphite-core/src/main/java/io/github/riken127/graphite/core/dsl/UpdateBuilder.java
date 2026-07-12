package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.PropertyTarget;
import io.github.riken127.graphite.core.model.UpdateQuery;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.validation.QueryValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Fluent builder for a MATCH-based update. */
public final class UpdateBuilder {

  private final PathPattern pathPattern;
  private final List<Predicate> predicates;
  private final Map<PropertyTarget, Object> assignments = new LinkedHashMap<>();
  private final List<PropertyTarget> removals = new ArrayList<>();
  private final List<String> projections = new ArrayList<>();

  UpdateBuilder(PathPattern pathPattern, List<Predicate> predicates) {
    this.pathPattern = Objects.requireNonNull(pathPattern, "pathPattern must not be null");
    this.predicates =
        List.copyOf(Objects.requireNonNull(predicates, "predicates must not be null"));
  }

  /** Assigns a value to a property on the first matched node. */
  public UpdateBuilder set(String property, Object value) {
    return set(pathPattern.start().alias(), property, value);
  }

  /** Assigns a value to an alias-qualified property. */
  public UpdateBuilder set(String alias, String property, Object value) {
    assignments.put(
        new PropertyTarget(alias, property),
        Objects.requireNonNull(value, "value must not be null"));
    return this;
  }

  /** Removes a property from the first matched node. */
  public UpdateBuilder remove(String property) {
    return remove(pathPattern.start().alias(), property);
  }

  /** Removes an alias-qualified property. */
  public UpdateBuilder remove(String alias, String property) {
    removals.add(new PropertyTarget(alias, property));
    return this;
  }

  /** Defines optional RETURN projections for the update. */
  public UpdateBuilder returning(String... expressions) {
    Objects.requireNonNull(expressions, "expressions must not be null");
    projections.clear();
    projections.addAll(Arrays.asList(expressions));
    return this;
  }

  /** Builds an immutable update query. */
  public UpdateQuery build() {
    UpdateQuery query =
        new UpdateQuery(pathPattern, predicates, assignments, removals, projections);
    QueryValidator.validate(query);
    return query;
  }
}
