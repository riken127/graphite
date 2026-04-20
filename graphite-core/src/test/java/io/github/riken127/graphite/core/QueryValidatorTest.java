package io.github.riken127.graphite.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class QueryValidatorTest {

  @Test
  void validateRejectsEmptyProjectionList() {
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
}
