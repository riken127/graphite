package io.github.riken127.graphite.examples;

import io.github.riken127.graphite.core.Graphite;
import io.github.riken127.graphite.core.MatchQuery;
import io.github.riken127.graphite.cypher.MatchQueryRenderer;
import io.github.riken127.graphite.cypher.RenderedQuery;
import io.github.riken127.graphite.metadata.NodeMetadata;

/** Minimal example showing intended module usage. */
public final class SampleUsage {

  private SampleUsage() {}

  /**
   * Example entry point.
   *
   * @param args CLI arguments
   */
  public static void main(String[] args) {
    NodeMetadata metadata = new NodeMetadata(Consultant.class, "Consultant");
    MatchQuery query =
        Graphite.match(Graphite.node(metadata.label()).as("c"))
            .where(Graphite.property("c", "id").eq("123"))
            .select("c")
            .orderBy(Graphite.desc("c", "createdAt"))
            .limit(5)
            .build();
    RenderedQuery rendered = new MatchQueryRenderer().render(query);

    System.out.println(rendered.cypher());
    System.out.println(rendered.parameters());
  }

  static final class Consultant {
    private Consultant() {}
  }
}
