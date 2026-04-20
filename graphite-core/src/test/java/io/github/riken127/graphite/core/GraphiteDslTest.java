package io.github.riken127.graphite.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GraphiteDslTest {

  @Test
  void buildCreatesValidatedMatchQuery() {
    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .select("c")
            .orderBy(Graphite.desc("c", "createdAt"))
            .limit(10)
            .build();

    assertEquals("Consultant", query.nodePattern().label());
    assertEquals("c", query.nodePattern().alias());
    assertEquals(1, query.predicates().size());
    assertEquals(1, query.projections().size());
    assertEquals(1, query.sorts().size());
    assertEquals(10, query.limit());
  }

  @Test
  void buildUsesNodeAliasWhenSelectIsMissing() {
    MatchQuery query = Graphite.match(Graphite.node("Consultant").as("c")).build();

    assertEquals(1, query.projections().size());
    assertEquals("c", query.projections().getFirst());
  }

  @Test
  void buildRejectsAliasMismatch() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Graphite.match(Graphite.node("Consultant").as("c"))
                .where(Graphite.property("x", "id").eq("42"))
                .select("c")
                .build());
  }
}
