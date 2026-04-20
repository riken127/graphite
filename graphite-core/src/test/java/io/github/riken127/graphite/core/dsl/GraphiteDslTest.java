package io.github.riken127.graphite.core.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphiteDslTest {

  @Test
  void matchSupportsAdvancedPredicatesAndPaging() {
    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .where(Graphite.property("c", "skills").in(List.of("java", "neo4j")))
            .where(Graphite.property("c", "name").startsWith("J"))
            .where(Graphite.property("c", "deletedAt").isNotNull())
            .select("c", "c.id")
            .orderBy(Graphite.desc("c", "createdAt"))
            .skip(20)
            .limit(10)
            .build();

    assertEquals("Consultant", query.nodePattern().label());
    assertEquals("c", query.nodePattern().alias());
    assertEquals(4, query.predicates().size());
    assertEquals(2, query.projections().size());
    assertEquals(1, query.sorts().size());
    assertEquals(20, query.skip());
    assertEquals(10, query.limit());
    assertEquals(InPredicate.class, query.predicates().get(1).getClass());
  }

  @Test
  void createSupportsBulkSetAndDefaultProjection() {
    CreateQuery query =
        Graphite.create(Graphite.node("Consultant").as("c"))
            .setAll(Map.of("id", "42", "active", true))
            .build();

    assertEquals(List.of("c"), query.projections());
    assertEquals("42", query.properties().get("id"));
    assertEquals(true, query.properties().get("active"));
  }

  @Test
  void mergeSupportsLifecycleSets() {
    MergeQuery query =
        Graphite.merge(Graphite.node("Consultant").as("c"))
            .on("id", "42")
            .onCreateSet("createdAt", "2026-04-20")
            .onMatchSet("lastSeen", "2026-04-20")
            .select("c")
            .build();

    assertEquals("42", query.identityProperties().get("id"));
    assertEquals("2026-04-20", query.onCreateProperties().get("createdAt"));
    assertEquals("2026-04-20", query.onMatchProperties().get("lastSeen"));
  }

  @Test
  void mergeRequiresIdentityProperties() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Graphite.merge(Graphite.node("Consultant").as("c")).select("c").build());
  }

  @Test
  void inPredicateRejectsEmptyValues() {
    assertThrows(
        IllegalArgumentException.class, () -> Graphite.property("c", "skills").in(List.of()));
  }
}
