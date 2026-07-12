package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.core.validation.AstValidator;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Idempotent Neo4j constraint and range-index operations with validated identifiers. */
public final class GraphiteSchemaManager {

  private final GraphiteClient client;
  private final QueryOptions writeOptions;

  public GraphiteSchemaManager(GraphiteClient client) {
    this(client, QueryOptions.builder().accessMode(QueryAccessMode.WRITE).build());
  }

  /** Creates a schema manager with explicit database and transaction defaults. */
  public GraphiteSchemaManager(GraphiteClient client, QueryOptions writeOptions) {
    this.client = Objects.requireNonNull(client, "client must not be null");
    QueryOptions supplied = Objects.requireNonNull(writeOptions, "writeOptions must not be null");
    this.writeOptions = QueryOptions.builder(supplied).accessMode(QueryAccessMode.WRITE).build();
  }

  /** Creates a named range index if it does not already exist. */
  public QuerySummary ensureRangeIndex(String name, String label, String... properties) {
    List<String> names = properties(properties);
    String cypher =
        "CREATE INDEX "
            + identifier(name)
            + " IF NOT EXISTS FOR (n:"
            + AstValidator.requireLabel(label)
            + ") ON ("
            + propertyList(names)
            + ")";
    return execute(cypher);
  }

  /** Creates a named uniqueness constraint if it does not already exist. */
  public QuerySummary ensureUniqueConstraint(String name, String label, String... properties) {
    List<String> names = properties(properties);
    String expression =
        names.size() == 1 ? "n." + names.getFirst() : "(" + propertyList(names) + ")";
    String cypher =
        "CREATE CONSTRAINT "
            + identifier(name)
            + " IF NOT EXISTS FOR (n:"
            + AstValidator.requireLabel(label)
            + ") REQUIRE "
            + expression
            + " IS UNIQUE";
    return execute(cypher);
  }

  /** Drops a named index when it exists. */
  public QuerySummary dropIndex(String name) {
    return execute("DROP INDEX " + identifier(name) + " IF EXISTS");
  }

  /** Drops a named constraint when it exists. */
  public QuerySummary dropConstraint(String name) {
    return execute("DROP CONSTRAINT " + identifier(name) + " IF EXISTS");
  }

  private QuerySummary execute(String cypher) {
    return client.execute(new RawCypherQuery(cypher, Map.of()), writeOptions).summary();
  }

  private static List<String> properties(String[] values) {
    Objects.requireNonNull(values, "properties must not be null");
    List<String> names = Arrays.stream(values).map(AstValidator::requireProperty).toList();
    if (names.isEmpty()) {
      throw new IllegalArgumentException("properties must not be empty");
    }
    return names;
  }

  private static String propertyList(List<String> properties) {
    return properties.stream()
        .map(property -> "n." + property)
        .reduce((a, b) -> a + ", " + b)
        .orElseThrow();
  }

  private static String identifier(String value) {
    return AstValidator.requireAlias(value);
  }
}
