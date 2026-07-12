package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CypherRendererTest {

  @Test
  void facadeDispatchesBuiltInQueries() {
    Query query = Graphite.create(Graphite.node("Consultant").as("c")).withoutReturn().build();

    assertEquals("CREATE (c:Consultant)", new CypherRenderer().render(query).cypher());
  }

  @Test
  void rawCypherKeepsTextAndParametersSeparate() {
    RawCypherQuery query =
        new RawCypherQuery("MATCH (n) WHERE n.id = $id RETURN n", Map.of("id", "42"));

    RenderedQuery rendered = new CypherRenderer().render(query);

    assertEquals(query.cypher(), rendered.cypher());
    assertEquals(query.parameters(), rendered.parameters());
  }

  @Test
  void facadeRejectsUnsupportedQueryType() {
    Query unsupported = new UnsupportedQuery();

    assertThrows(IllegalArgumentException.class, () -> new CypherRenderer().render(unsupported));
  }

  @Test
  void facadeAcceptsAnAdditionalQueryRenderer() {
    CypherRenderer renderer = new CypherRenderer().withRenderer(new UnsupportedQueryRenderer());

    assertEquals("RETURN 1", renderer.render(new UnsupportedQuery()).cypher());
  }

  private record UnsupportedQuery() implements Query {}

  private static final class UnsupportedQueryRenderer implements QueryRenderer<UnsupportedQuery> {

    @Override
    public Class<UnsupportedQuery> queryType() {
      return UnsupportedQuery.class;
    }

    @Override
    public RenderedQuery render(UnsupportedQuery query) {
      return new RenderedQuery("RETURN 1", Map.of());
    }
  }
}
