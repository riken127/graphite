package example;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import io.github.riken127.graphite.cypher.renderer.CypherRenderer;

/** Compiles the documented Java query and rendering path outside the Graphite reactor. */
public final class JavaConsumer {

  private JavaConsumer() {}

  public static RenderedQuery render() {
    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "active").eq(true))
            .select("c")
            .limit(10)
            .build();
    return new CypherRenderer().render(query);
  }
}
