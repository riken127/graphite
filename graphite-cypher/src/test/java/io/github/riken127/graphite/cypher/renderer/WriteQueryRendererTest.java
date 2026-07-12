package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.model.UpdateQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WriteQueryRendererTest {

  @Test
  void renderUpdateSupportsSetRemoveAndOptionalReturn() {
    UpdateQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .update()
            .set("name", " Julia ")
            .remove("legacyName")
            .returning("c")
            .build();

    RenderedQuery rendered = new UpdateQueryRenderer().render(query);

    assertEquals(
        "MATCH (c:Consultant) WHERE c.id = $p0 SET c.name = $p1 REMOVE c.legacyName RETURN c",
        rendered.cypher());
    assertEquals(Map.of("p0", "42", "p1", " Julia "), rendered.parameters());
  }

  @Test
  void renderDeleteSupportsRelationshipAlias() {
    DeleteQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .out("REFERRED_BY")
            .as("ref")
            .to(Graphite.node("Consultant").as("m"))
            .where(Graphite.property("c", "id").eq("42"))
            .delete("ref")
            .build();

    RenderedQuery rendered = new DeleteQueryRenderer().render(query);

    assertEquals(
        "MATCH (c:Consultant)-[ref:REFERRED_BY]->(m:Consultant) WHERE c.id = $p0 DELETE ref",
        rendered.cypher());
    assertEquals(Map.of("p0", "42"), rendered.parameters());
  }
}
