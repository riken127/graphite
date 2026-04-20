package io.github.riken127.graphite.cypher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.Graphite;
import io.github.riken127.graphite.core.MergeQuery;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MergeQueryRendererTest {

  private final MergeQueryRenderer renderer = new MergeQueryRenderer();

  @Test
  void renderIncludesIdentityProperties() {
    MergeQuery query =
        Graphite.merge(Graphite.node("Consultant").as("c"))
            .on("id", "42")
            .on("tenant", "acme")
            .select("c")
            .build();

    RenderedQuery rendered = renderer.render(query);

    assertEquals("MERGE (c:Consultant {id: $p0, tenant: $p1}) RETURN c", rendered.cypher());
    assertEquals(Map.of("p0", "42", "p1", "acme"), rendered.parameters());
  }

  @Test
  void renderRejectsMissingIdentity() {
    MergeQuery query = new MergeQuery(Graphite.node("Consultant").as("c"), Map.of(), List.of("c"));

    assertThrows(IllegalArgumentException.class, () -> renderer.render(query));
  }
}
