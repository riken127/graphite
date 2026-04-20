package io.github.riken127.graphite.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class QueryValidatorTest {

  @Test
  void validateRejectsEmptyProjectionAlias() {
    MatchQuery query =
        new MatchQuery(
            new NodePattern("Consultant", "c"), List.of(), List.of("c"), List.of(), null);

    MatchQuery invalid =
        new MatchQuery(
            query.nodePattern(), query.predicates(), List.of(""), query.sorts(), query.limit());

    assertThrows(IllegalArgumentException.class, () -> QueryValidator.validate(invalid));
  }

  @Test
  void limitMustBePositive() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MatchQuery(
                new NodePattern("Consultant", "c"), List.of(), List.of("c"), List.of(), 0));
  }

  @Test
  void validateRejectsMergeWithoutIdentityProperties() {
    MergeQuery query = new MergeQuery(new NodePattern("Consultant", "c"), Map.of(), List.of("c"));

    assertThrows(IllegalArgumentException.class, () -> QueryValidator.validate(query));
  }

  @Test
  void createQueryRejectsNullPropertyValue() {
    assertThrows(
        NullPointerException.class,
        () ->
            new CreateQuery(new NodePattern("Consultant", "c"), Map.of("id", null), List.of("c")));
  }
}
