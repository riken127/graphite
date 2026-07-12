package io.github.riken127.graphite.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.QueryAccessMode;
import io.github.riken127.graphite.neo4j.QueryOptions;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.neo4j.Neo4jContainer;

@Testcontainers(disabledWithoutDocker = true)
class GraphiteSpringIntegrationTest {

  @Container
  private static final Neo4jContainer NEO4J =
      new Neo4jContainer(System.getProperty("graphite.neo4j.image", "neo4j:5.26-community"))
          .withoutAuthentication();

  private static Driver driver;
  private static GraphiteClient client;
  private static GraphiteSpringTemplate template;
  private static TransactionTemplate transactions;

  @BeforeAll
  static void connect() {
    driver = GraphDatabase.driver(NEO4J.getBoltUrl(), AuthTokens.none());
    client = new GraphiteClient(driver);
    template = new GraphiteSpringTemplate(client);
    transactions = new TransactionTemplate(new GraphiteTransactionManager(client));
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
  void participatesInRealCommitAndRollbackTransactions() {
    transactions.executeWithoutResult(
        ignored ->
            template.execute(
                new RawCypherQuery("CREATE (:Consultant {id: $id})", Map.of("id", "1"))));
    assertEquals(1, countConsultants());

    assertThrows(
        RollbackSignal.class,
        () ->
            transactions.executeWithoutResult(
                ignored -> {
                  template.execute(
                      new RawCypherQuery("CREATE (:Consultant {id: $id})", Map.of("id", "2")));
                  throw new RollbackSignal();
                }));
    assertEquals(1, countConsultants());
  }

  private int countConsultants() {
    return client
        .execute(
            new RawCypherQuery("MATCH (c:Consultant) RETURN count(c) AS count", Map.of()),
            row -> row.value("count").asInt(),
            QueryOptions.builder().accessMode(QueryAccessMode.READ).build())
        .single();
  }

  private static final class RollbackSignal extends RuntimeException {}
}
