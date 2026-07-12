package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;

/** Query operations available both directly and inside managed transactions. */
public interface GraphiteOperations {

  QueryResult<Neo4jRecord> execute(Query query);

  <T> QueryResult<T> execute(Query query, Neo4jRecordMapper<T> mapper);

  QueryResult<Neo4jRecord> execute(RawCypherQuery query);

  <T> QueryResult<T> execute(RawCypherQuery query, Neo4jRecordMapper<T> mapper);
}
