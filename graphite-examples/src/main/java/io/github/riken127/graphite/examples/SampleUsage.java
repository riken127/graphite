package io.github.riken127.graphite.examples;

import io.github.riken127.graphite.core.CreateQuery;
import io.github.riken127.graphite.core.Graphite;
import io.github.riken127.graphite.core.MatchQuery;
import io.github.riken127.graphite.core.MergeQuery;
import io.github.riken127.graphite.cypher.CreateQueryRenderer;
import io.github.riken127.graphite.cypher.MatchQueryRenderer;
import io.github.riken127.graphite.cypher.MergeQueryRenderer;
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
    CreateQuery createQuery =
        Graphite.create(Graphite.node(metadata.label()).as("c"))
            .set("id", "123")
            .set("name", "Julia")
            .build();
    MergeQuery mergeQuery =
        Graphite.merge(Graphite.node(metadata.label()).as("c"))
            .on("id", "123")
            .on("tenant", "acme")
            .build();

    RenderedQuery matchRendered = new MatchQueryRenderer().render(query);
    RenderedQuery createRendered = new CreateQueryRenderer().render(createQuery);
    RenderedQuery mergeRendered = new MergeQueryRenderer().render(mergeQuery);

    System.out.println(matchRendered.cypher());
    System.out.println(matchRendered.parameters());
    System.out.println(createRendered.cypher());
    System.out.println(createRendered.parameters());
    System.out.println(mergeRendered.cypher());
    System.out.println(mergeRendered.parameters());
  }

  static final class Consultant {
    private Consultant() {}
  }
}
