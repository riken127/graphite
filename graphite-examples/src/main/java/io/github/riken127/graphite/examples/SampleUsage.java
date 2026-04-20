package io.github.riken127.graphite.examples;

import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import io.github.riken127.graphite.cypher.renderer.CreateQueryRenderer;
import io.github.riken127.graphite.cypher.renderer.MatchQueryRenderer;
import io.github.riken127.graphite.cypher.renderer.MergeQueryRenderer;
import io.github.riken127.graphite.metadata.NodeMetadata;
import java.util.List;

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

    MatchQuery matchQuery =
        Graphite.match(Graphite.node(metadata.label()).as("c"))
            .where(Graphite.property("c", "id").eq("123"))
            .where(Graphite.property("c", "skills").in(List.of("java", "neo4j")))
            .where(Graphite.property("c", "deletedAt").isNull())
            .select("c", "c.id")
            .orderBy(Graphite.desc("c", "createdAt"))
            .skip(0)
            .limit(25)
            .build();

    CreateQuery createQuery =
        Graphite.create(Graphite.node(metadata.label()).as("c"))
            .set("id", "123")
            .set("name", "Julia")
            .set("active", true)
            .build();

    MergeQuery mergeQuery =
        Graphite.merge(Graphite.node(metadata.label()).as("c"))
            .on("id", "123")
            .on("tenant", "acme")
            .onCreateSet("createdAt", "2026-04-20")
            .onMatchSet("lastSeen", "2026-04-20")
            .build();

    RenderedQuery matchRendered = new MatchQueryRenderer().render(matchQuery);
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
