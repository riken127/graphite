package io.github.riken127.graphite.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Map;
import java.util.Objects;

/** Focused JUnit assertions for rendered Graphite queries. */
public final class GraphiteAssertions {

  private GraphiteAssertions() {}

  /** Asserts both rendered Cypher and its separate parameter map. */
  public static void assertRendered(
      RenderedQuery actual, String expectedCypher, Map<String, ?> expectedParameters) {
    Objects.requireNonNull(actual, "actual must not be null");
    assertEquals(expectedCypher, actual.cypher(), "rendered Cypher");
    assertEquals(expectedParameters, actual.parameters(), "query parameters");
  }
}
