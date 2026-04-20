package io.github.riken127.graphite.cypher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.Graphite;
import io.github.riken127.graphite.core.MatchQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MatchQueryRendererTest {

  private final MatchQueryRenderer renderer = new MatchQueryRenderer();

  @Test
  void renderIncludesWhereOrderByAndLimit() {
    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .where(Graphite.property("c", "active").eq(true))
            .select("c")
            .orderBy(Graphite.desc("c", "createdAt"))
            .limit(10)
            .build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals(
        "MATCH (c:Consultant) WHERE c.id = $p0 AND c.active = $p1 RETURN c ORDER BY c.createdAt"
            + " DESC LIMIT 10",
        rendered.cypher());
    assertEquals(Map.of("p0", "42", "p1", true), rendered.parameters());
  }

  @Test
  void renderWithoutWhereOrLimit() {
    MatchQuery query = Graphite.match(Graphite.node("Consultant").as("c")).build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("MATCH (c:Consultant) RETURN c", rendered.cypher());
    assertEquals(Map.of(), rendered.parameters());
  }
}
