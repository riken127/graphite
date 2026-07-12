package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import io.github.riken127.graphite.cypher.renderer.CypherRenderer;
import io.github.riken127.graphite.neo4j.exception.GraphiteMappingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.SimpleQueryRunner;
import org.neo4j.driver.exceptions.Neo4jException;

final class DriverQueryExecutor implements GraphiteOperations {

  private final SimpleQueryRunner queryRunner;
  private final CypherRenderer renderer;
  private final QueryObserver observer;

  DriverQueryExecutor(SimpleQueryRunner queryRunner, CypherRenderer renderer) {
    this(queryRunner, renderer, QueryObserver.noop());
  }

  DriverQueryExecutor(
      SimpleQueryRunner queryRunner, CypherRenderer renderer, QueryObserver observer) {
    this.queryRunner = Objects.requireNonNull(queryRunner, "queryRunner must not be null");
    this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    this.observer = Objects.requireNonNull(observer, "observer must not be null");
  }

  @Override
  public QueryResult<Neo4jRecord> execute(Query query) {
    return execute(query, record -> record);
  }

  @Override
  public <T> QueryResult<T> execute(Query query, Neo4jRecordMapper<T> mapper) {
    return executeRendered(renderer.render(query), mapper);
  }

  @Override
  public QueryResult<Neo4jRecord> execute(RawCypherQuery query) {
    return execute(query, record -> record);
  }

  @Override
  public <T> QueryResult<T> execute(RawCypherQuery query, Neo4jRecordMapper<T> mapper) {
    return executeRendered(renderer.render(query), mapper);
  }

  <T> QueryResult<T> executeRendered(RenderedQuery renderedQuery, Neo4jRecordMapper<T> mapper) {
    Objects.requireNonNull(renderedQuery, "renderedQuery must not be null");
    Objects.requireNonNull(mapper, "mapper must not be null");
    QueryObservation observation =
        QueryObservations.begin(observer, QueryObservations.descriptor(renderedQuery, false));

    try {
      Result result = queryRunner.run(renderedQuery.cypher(), renderedQuery.parameters());
      List<T> mappedRecords = new ArrayList<>();
      while (result.hasNext()) {
        Record record = result.next();
        try {
          mappedRecords.add(mapper.map(new Neo4jRecord(record)));
        } catch (Neo4jException failure) {
          throw failure;
        } catch (RuntimeException failure) {
          throw new GraphiteMappingException("failed to map Neo4j result record", failure);
        }
      }

      QueryResult<T> mappedResult =
          new QueryResult<>(mappedRecords, DriverSummaryMapper.map(result.consume()), Set.of());
      QueryObservations.succeeded(observation, mappedResult.summary(), mappedRecords.size());
      return mappedResult;
    } catch (RuntimeException failure) {
      RuntimeException translated = DriverExceptionTranslator.translate(failure);
      QueryObservations.failed(observation, translated);
      throw translated;
    }
  }
}
