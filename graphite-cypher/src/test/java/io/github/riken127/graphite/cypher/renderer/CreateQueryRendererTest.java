package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CreateQueryRendererTest {

  private final CreateQueryRenderer renderer = new CreateQueryRenderer();

  @Test
  void renderIncludesProperties() {
    CreateQuery query =
        Graphite.create(Graphite.node("Consultant").as("c"))
            .set("id", "42")
            .set("active", true)
            .select("c")
            .build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("CREATE (c:Consultant {id: $p0, active: $p1}) RETURN c", rendered.cypher());
    assertEquals(Map.of("p0", "42", "p1", true), rendered.parameters());
  }

  @Test
  void renderWithoutProperties() {
    CreateQuery query = Graphite.create(Graphite.node("Consultant").as("c")).select("c.id").build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("CREATE (c:Consultant) RETURN c.id", rendered.cypher());
    assertEquals(Map.of(), rendered.parameters());
  }
}
