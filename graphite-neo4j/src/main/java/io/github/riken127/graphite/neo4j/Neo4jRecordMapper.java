package io.github.riken127.graphite.neo4j;

/** Maps one Neo4j result record into an application value. */
@FunctionalInterface
public interface Neo4jRecordMapper<T> {

  T map(Neo4jRecord record);
}
