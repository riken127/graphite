package io.github.riken127.graphite.test;

import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import java.util.Map;
import java.util.Objects;
import org.testcontainers.neo4j.Neo4jContainer;

/** Shared Neo4j Testcontainers and database-cleanup helpers. */
public final class Neo4jTestSupport {

  private Neo4jTestSupport() {}

  /** Creates an unstarted Neo4j container using the configurable compatibility-test image. */
  public static Neo4jContainer container() {
    return new Neo4jContainer(System.getProperty("graphite.neo4j.image", "neo4j:5.26-community"))
        .withoutAuthentication();
  }

  /** Removes every node and relationship from the client's configured database. */
  public static void clearDatabase(GraphiteClient client) {
    Objects.requireNonNull(client, "client must not be null");
    client.execute(new RawCypherQuery("MATCH (n) DETACH DELETE n", Map.of()));
  }
}
