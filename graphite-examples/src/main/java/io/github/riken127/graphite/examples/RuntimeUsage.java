package io.github.riken127.graphite.examples;

import io.github.riken127.graphite.core.dsl.Expressions;
import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.metadata.GraphEntity;
import io.github.riken127.graphite.metadata.GraphEntityFactory;
import io.github.riken127.graphite.metadata.GraphId;
import io.github.riken127.graphite.metadata.GraphNode;
import io.github.riken127.graphite.metadata.RecordEntityMapper;
import io.github.riken127.graphite.metadata.ReflectionNodeMetadataRegistry;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.Neo4jMappers;
import io.github.riken127.graphite.neo4j.StreamingQueryResult;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

/** Runnable metadata-backed streaming example configured through Neo4j environment variables. */
public final class RuntimeUsage {

  private RuntimeUsage() {}

  /** Connects to Neo4j and streams consultants ordered by name. */
  public static void main(String[] args) {
    ReflectionNodeMetadataRegistry metadata = new ReflectionNodeMetadataRegistry();
    GraphEntity<Consultant> consultant =
        new GraphEntityFactory(metadata).entity(Consultant.class, "c");
    ClauseQuery query =
        Graphite.query()
            .match(Graphite.path(consultant.node()).build())
            .returning(Projection.of(consultant.variable()))
            .orderBy(Expressions.asc(consultant.property("name", String.class)))
            .build();

    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost:7687");
    try (Driver driver = GraphDatabase.driver(uri, authentication())) {
      GraphiteClient client = new GraphiteClient(driver);
      RecordEntityMapper mapper = new RecordEntityMapper(metadata);
      try (StreamingQueryResult<Consultant> consultants =
          client.stream(query, Neo4jMappers.node("c", Consultant.class, mapper))) {
        consultants.forEachRemaining(System.out::println);
      }
    }
  }

  private static AuthToken authentication() {
    String username = System.getenv("NEO4J_USERNAME");
    if (username == null || username.isBlank()) {
      return AuthTokens.none();
    }
    String password = System.getenv("NEO4J_PASSWORD");
    if (password == null) {
      throw new IllegalArgumentException("NEO4J_PASSWORD is required with NEO4J_USERNAME");
    }
    return AuthTokens.basic(username, password);
  }

  @GraphNode("Consultant")
  private record Consultant(@GraphId String id, String name) {}
}
