package io.github.riken127.graphite.core.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.NodePattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QueryValidatorTest {

  @Test
  void validateRejectsProjectionAliasMismatch() {
    MatchQuery query =
        new MatchQuery(
            new NodePattern("Consultant", "c"), List.of(), List.of("x"), List.of(), null, null);

    assertThrows(IllegalArgumentException.class, () -> QueryValidator.validate(query));
  }

  @Test
  void validateAcceptsPropertyProjection() {
    MatchQuery query = Graphite.match(Graphite.node("Consultant").as("c")).select("c.id").build();

    assertDoesNotThrow(() -> QueryValidator.validate(query));
  }

  @Test
  void validateRejectsMergeWithoutIdentityProperties() {
    MergeQuery query =
        new MergeQuery(
            new NodePattern("Consultant", "c"), Map.of(), Map.of(), Map.of(), List.of("c"));

    assertThrows(IllegalArgumentException.class, () -> QueryValidator.validate(query));
  }

  @Test
  void matchSkipMustBeNonNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MatchQuery(
                new NodePattern("Consultant", "c"), List.of(), List.of("c"), List.of(), -1, 10));
  }

  @Test
  void createQueryRejectsNullPropertyValue() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("id", null);

    assertThrows(
        NullPointerException.class,
        () -> new CreateQuery(new NodePattern("Consultant", "c"), properties, List.of("c")));
  }
}
