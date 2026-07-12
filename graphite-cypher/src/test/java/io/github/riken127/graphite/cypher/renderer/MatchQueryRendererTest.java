package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MatchQueryRendererTest {

  private final MatchQueryRenderer renderer = new MatchQueryRenderer();

  @Test
  void renderIncludesAdvancedPredicatesAndPaging() {
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

    RenderedQuery rendered = renderer.render(query);

    assertEquals(
        "MATCH (c:Consultant) WHERE c.id = $p0 AND c.skills IN $p1 AND c.name STARTS WITH $p2 "
            + "AND c.deletedAt IS NOT NULL RETURN c, c.id ORDER BY c.createdAt DESC SKIP $p3 "
            + "LIMIT $p4",
        rendered.cypher());
    assertEquals(
        Map.of("p0", "42", "p1", List.of("java", "neo4j"), "p2", "J", "p3", 20, "p4", 10),
        rendered.parameters());
    assertEquals(
        List.of("p0", "p1", "p2", "p3", "p4"), rendered.parameters().keySet().stream().toList());
  }

  @Test
  void renderSupportsRelationshipTraversalAndGroupedPredicates() {
    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .out("REFERRED_BY")
            .as("ref")
            .hops(1, 3)
            .to(Graphite.node("Consultant").as("m"))
            .where(
                Graphite.property("c", "active")
                    .eq(true)
                    .and(Graphite.property("m", "name").startsWith(" J")))
            .select("m", "ref")
            .build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals(
        "MATCH (c:Consultant)-[ref:REFERRED_BY*1..3]->(m:Consultant) "
            + "WHERE (c.active = $p0 AND m.name STARTS WITH $p1) RETURN m, ref",
        rendered.cypher());
    assertEquals(Map.of("p0", true, "p1", " J"), rendered.parameters());
  }

  @Test
  void renderWithoutWhereOrPaging() {
    MatchQuery query = Graphite.match(Graphite.node("Consultant").as("c")).select("c.id").build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("MATCH (c:Consultant) RETURN c.id", rendered.cypher());
    assertEquals(Map.of(), rendered.parameters());
  }
}
