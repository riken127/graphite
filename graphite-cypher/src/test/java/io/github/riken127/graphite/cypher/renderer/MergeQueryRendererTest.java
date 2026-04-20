package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MergeQueryRendererTest {

  private final MergeQueryRenderer renderer = new MergeQueryRenderer();

  @Test
  void renderIncludesIdentityAndLifecycleSets() {
    MergeQuery query =
        Graphite.merge(Graphite.node("Consultant").as("c"))
            .on("id", "42")
            .on("tenant", "acme")
            .onCreateSet("name", "Julia")
            .onCreateSet("createdAt", "2026-04-20")
            .onMatchSet("lastSeen", "2026-04-20")
            .select("c")
            .build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals(
        "MERGE (c:Consultant {id: $p0, tenant: $p1}) ON CREATE SET c.name = $p2, "
            + "c.createdAt = $p3 ON MATCH SET c.lastSeen = $p4 RETURN c",
        rendered.cypher());
    assertEquals(
        Map.of("p0", "42", "p1", "acme", "p2", "Julia", "p3", "2026-04-20", "p4", "2026-04-20"),
        rendered.parameters());
  }

  @Test
  void renderWithoutLifecycleSets() {
    MergeQuery query = Graphite.merge(Graphite.node("Consultant").as("c")).on("id", "42").build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("MERGE (c:Consultant {id: $p0}) RETURN c", rendered.cypher());
    assertEquals(Map.of("p0", "42"), rendered.parameters());
  }
}
