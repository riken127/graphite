package io.github.riken127.graphite.spring;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.GraphiteOperations;
import io.github.riken127.graphite.neo4j.Neo4jRecord;
import io.github.riken127.graphite.neo4j.Neo4jRecordMapper;
import io.github.riken127.graphite.neo4j.QueryResult;
import java.util.Objects;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Spring-aware query facade that participates in Graphite-managed transactions. */
public final class GraphiteSpringTemplate implements GraphiteOperations {

  private final GraphiteClient client;

  public GraphiteSpringTemplate(GraphiteClient client) {
    this.client = Objects.requireNonNull(client, "client must not be null");
  }

  @Override
  public QueryResult<Neo4jRecord> execute(Query query) {
    return currentOperations().execute(query);
  }

  @Override
  public <T> QueryResult<T> execute(Query query, Neo4jRecordMapper<T> mapper) {
    return currentOperations().execute(query, mapper);
  }

  @Override
  public QueryResult<Neo4jRecord> execute(RawCypherQuery query) {
    return currentOperations().execute(query);
  }

  @Override
  public <T> QueryResult<T> execute(RawCypherQuery query, Neo4jRecordMapper<T> mapper) {
    return currentOperations().execute(query, mapper);
  }

  private GraphiteOperations currentOperations() {
    Object resource = TransactionSynchronizationManager.getResource(client);
    if (resource instanceof GraphiteTransactionHolder holder) {
      return holder.transaction();
    }
    return client;
  }
}
