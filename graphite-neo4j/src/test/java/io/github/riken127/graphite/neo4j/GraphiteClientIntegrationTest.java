package io.github.riken127.graphite.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.riken127.graphite.core.dsl.Expressions;
import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.CreateQuery;
import io.github.riken127.graphite.core.model.DeleteQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.MergeQuery;
import io.github.riken127.graphite.core.model.UpdateQuery;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.metadata.GraphId;
import io.github.riken127.graphite.metadata.GraphNode;
import io.github.riken127.graphite.metadata.RecordEntityMapper;
import io.github.riken127.graphite.metadata.ReflectionNodeMetadataRegistry;
import io.github.riken127.graphite.neo4j.exception.GraphiteClientException;
import io.github.riken127.graphite.neo4j.exception.GraphiteResultCardinalityException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.neo4j.Neo4jContainer;

@Testcontainers(disabledWithoutDocker = true)
class GraphiteClientIntegrationTest {

  @Container
  private static final Neo4jContainer NEO4J =
      new Neo4jContainer(System.getProperty("graphite.neo4j.image", "neo4j:5.26-community"))
          .withoutAuthentication();

  private static Driver driver;
  private static GraphiteClient client;

  @BeforeAll
  static void connect() {
    driver = GraphDatabase.driver(NEO4J.getBoltUrl(), AuthTokens.none());
    client = new GraphiteClient(driver);
    client.verifyConnectivity();
  }

  @AfterAll
  static void disconnect() {
    if (driver != null) {
      driver.close();
    }
  }

  @BeforeEach
  void clearDatabase() {
    client.execute(new RawCypherQuery("MATCH (n) DETACH DELETE n", Map.of()));
  }

  @Test
  void executesCreateMatchUpdateMergeAndDeleteQueries() {
    CreateQuery create =
        Graphite.create(Graphite.node("Consultant").as("c"))
            .set("id", "42")
            .set("name", "Julia")
            .set("birthday", LocalDate.of(1990, 3, 4))
            .withoutReturn()
            .build();

    QueryResult<Neo4jRecord> created =
        client.execute(
            create,
            QueryOptions.builder()
                .timeout(Duration.ofSeconds(5))
                .metadata(Map.of("operation", "create-consultant"))
                .build());
    assertEquals(1, created.summary().counters().nodesCreated());
    assertFalse(created.bookmarks().isEmpty());

    MatchQuery match =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .select("c.name", "c.birthday")
            .build();
    ConsultantView consultant =
        client
            .execute(
                match,
                row ->
                    new ConsultantView(
                        row.value("c.name").asString(), row.value("c.birthday").asLocalDate()))
            .single();
    assertEquals(new ConsultantView("Julia", LocalDate.of(1990, 3, 4)), consultant);

    UpdateQuery update =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .update()
            .set("name", "Juliana")
            .remove("birthday")
            .returning("c.name")
            .build();
    QueryResult<String> updated = client.execute(update, row -> row.value("c.name").asString());
    assertEquals("Juliana", updated.single());
    assertEquals(2, updated.summary().counters().propertiesSet());

    MergeQuery merge =
        Graphite.merge(Graphite.node("Consultant").as("c"))
            .on("id", "42")
            .onMatchSet("active", true)
            .withoutReturn()
            .build();
    assertEquals(1, client.execute(merge).summary().counters().propertiesSet());

    DeleteQuery delete =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .where(Graphite.property("c", "id").eq("42"))
            .detachDelete()
            .build();
    assertEquals(1, client.execute(delete).summary().counters().nodesDeleted());
  }

  @Test
  void executesRelationshipTraversalAgainstNeo4j() {
    client.execute(
        new RawCypherQuery(
            "CREATE (:Consultant {id: $from})-[:REFERRED_BY {since: $since}]->"
                + "(:Consultant {id: $to})",
            Map.of("from", "1", "to", "2", "since", 2024)));

    MatchQuery query =
        Graphite.match(Graphite.node("Consultant").as("c"))
            .out("REFERRED_BY")
            .as("ref")
            .to(Graphite.node("Consultant").as("m"))
            .where(Graphite.property("c", "id").eq("1"))
            .select("m.id", "ref")
            .build();

    RelationshipView relationship =
        client
            .execute(
                query,
                row ->
                    new RelationshipView(
                        row.value("m.id").asString(),
                        row.value("ref").asRelationship().type(),
                        row.value("ref").asRelationship().get("since").asInt()))
            .single();
    assertEquals(new RelationshipView("2", "REFERRED_BY", 2024), relationship);
  }

  @Test
  void mapsReturnedNodesIntoAnnotatedRecords() {
    client.execute(
        Graphite.create(Graphite.node("MappedConsultant").as("c"))
            .set("id", "42")
            .set("name", "Julia")
            .build());
    RecordEntityMapper entityMapper = new RecordEntityMapper(new ReflectionNodeMetadataRegistry());

    MappedConsultant consultant =
        client
            .execute(
                Graphite.match(Graphite.node("MappedConsultant").as("c"))
                    .where(Graphite.property("c", "id").eq("42"))
                    .select("c")
                    .build(),
                Neo4jMappers.node("c", MappedConsultant.class, entityMapper))
            .single();

    assertEquals(new MappedConsultant("42", "Julia"), consultant);
  }

  @Test
  void commitsAndRollsBackManagedTransactions() {
    TransactionResult<Void> committed =
        client.writeTransaction(
            operations -> {
              operations.execute(createConsultant("1"));
              operations.execute(createConsultant("2"));
              return null;
            });
    assertFalse(committed.bookmarks().isEmpty());
    assertEquals(2, consultantCount());

    assertThrows(
        RollbackSignal.class,
        () ->
            client.writeTransaction(
                operations -> {
                  operations.execute(createConsultant("3"));
                  throw new RollbackSignal();
                }));
    assertEquals(2, consultantCount());
  }

  @Test
  void supportsExplicitCommitAndCloseRollback() {
    GraphiteExplicitTransaction committed =
        client.beginTransaction(QueryAccessMode.WRITE, QueryOptions.defaults());
    committed.execute(createConsultant("1"));
    committed.commit();
    assertFalse(committed.bookmarks().isEmpty());
    assertEquals(1, consultantCount());

    try (GraphiteExplicitTransaction rolledBack =
        client.beginTransaction(QueryAccessMode.WRITE, QueryOptions.defaults())) {
      rolledBack.execute(createConsultant("2"));
    }
    assertEquals(1, consultantCount());
  }

  @Test
  void mapsDriverFailuresAndEnforcesResultCardinality() {
    assertThrows(
        GraphiteClientException.class,
        () ->
            client.execute(
                new RawCypherQuery("THIS IS NOT CYPHER", Map.of()),
                QueryOptions.builder().accessMode(QueryAccessMode.READ).build()));

    try (GraphiteExplicitTransaction transaction =
        client.beginTransaction(QueryAccessMode.READ, QueryOptions.defaults())) {
      assertThrows(
          GraphiteClientException.class,
          () -> transaction.execute(new RawCypherQuery("THIS IS NOT CYPHER", Map.of())));
    }

    client.execute(createConsultant("1"));
    client.execute(createConsultant("2"));
    QueryResult<Neo4jRecord> result =
        client.execute(
            new RawCypherQuery("MATCH (c:Consultant) RETURN c", Map.of()),
            QueryOptions.builder().accessMode(QueryAccessMode.READ).build());
    assertThrows(GraphiteResultCardinalityException.class, result::single);
    assertThrows(GraphiteResultCardinalityException.class, result::optional);
  }

  @Test
  void streamsResultsAndReportsSuccessfulObservation() {
    client.execute(createConsultant("1"));
    client.execute(createConsultant("2"));
    AtomicReference<QueryDescriptor> descriptor = new AtomicReference<>();
    AtomicBoolean succeeded = new AtomicBoolean();
    GraphiteClient observedClient =
        new GraphiteClient(
            driver,
            new io.github.riken127.graphite.cypher.renderer.CypherRenderer(),
            QueryOptions.defaults(),
            query -> {
              descriptor.set(query);
              return new QueryObservation() {
                @Override
                public void succeeded(QuerySummary summary, long recordCount) {
                  succeeded.set(recordCount == 2);
                }
              };
            });

    try (StreamingQueryResult<String> result =
        observedClient.stream(
            new RawCypherQuery("MATCH (c:Consultant) RETURN c.id AS id ORDER BY id", Map.of()),
            row -> row.value("id").asString(),
            QueryOptions.builder().accessMode(QueryAccessMode.READ).build())) {
      assertEquals(List.of("1", "2"), result.stream().toList());
      assertTrue(result.completed());
      assertTrue(result.summary().isPresent());
      assertFalse(result.bookmarks().isEmpty());
    }

    assertEquals("MATCH", descriptor.get().operation());
    assertTrue(descriptor.get().streaming());
    assertTrue(succeeded.get());
  }

  @Test
  void rollsBackPartiallyConsumedWriteStreamOnClose() {
    StreamingQueryResult<String> result =
        client.stream(
            new RawCypherQuery(
                "UNWIND $ids AS id CREATE (c:Consultant {id: id}) RETURN c.id AS id",
                Map.of("ids", List.of("1", "2"))),
            row -> row.value("id").asString(),
            QueryOptions.builder().accessMode(QueryAccessMode.WRITE).build());

    assertEquals("1", result.next());
    result.close();

    assertFalse(result.completed());
    assertEquals(0, consultantCount());
  }

  @Test
  void managesIdempotentIndexesAndConstraints() {
    GraphiteSchemaManager schema = new GraphiteSchemaManager(client);

    assertEquals(
        1,
        schema
            .ensureUniqueConstraint("consultant_id_unique", "Consultant", "id")
            .counters()
            .constraintsAdded());
    assertEquals(
        0,
        schema
            .ensureUniqueConstraint("consultant_id_unique", "Consultant", "id")
            .counters()
            .constraintsAdded());
    assertEquals(
        1,
        schema
            .ensureRangeIndex("consultant_name_index", "Consultant", "name")
            .counters()
            .indexesAdded());

    schema.dropIndex("consultant_name_index");
    schema.dropConstraint("consultant_id_unique");
  }

  @Test
  void executesStructuredWritesSubqueriesAndUnions() {
    ClauseQuery create =
        Graphite.query()
            .create(Graphite.path(Graphite.node("Person").as("p")).build())
            .set(
                Expressions.set(
                    Graphite.property("p", "name", String.class),
                    Expressions.value("Ada", String.class)))
            .build();

    assertEquals(1, client.execute(create).summary().counters().nodesCreated());

    ClauseQuery nested =
        Graphite.subquery("p")
            .returning(Projection.as(Graphite.property("p", "name", String.class), "entityName"))
            .build();
    ClauseQuery people =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Person").as("p")).build())
            .subquery(nested, List.of("p"), "entityName")
            .returning(Projection.as(Expressions.variable("entityName", String.class), "name"))
            .build();
    ClauseQuery companies =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Company").as("c")).build())
            .returning(Projection.as(Graphite.property("c", "name", String.class), "name"))
            .build();

    assertEquals(
        List.of("Ada"),
        client
            .execute(Graphite.unionAll(people, companies), row -> row.value("name").asString())
            .records());
  }

  private static CreateQuery createConsultant(String id) {
    return Graphite.create(Graphite.node("Consultant").as("c"))
        .set("id", id)
        .withoutReturn()
        .build();
  }

  private static int consultantCount() {
    return client
        .execute(
            new RawCypherQuery("MATCH (c:Consultant) RETURN count(c) AS count", Map.of()),
            row -> row.value("count").asInt(),
            QueryOptions.builder().accessMode(QueryAccessMode.READ).build())
        .single();
  }

  private record ConsultantView(String name, LocalDate birthday) {}

  private record RelationshipView(String targetId, String type, int since) {}

  @GraphNode("MappedConsultant")
  private record MappedConsultant(@GraphId String id, String name) {}

  private static final class RollbackSignal extends RuntimeException {}
}
