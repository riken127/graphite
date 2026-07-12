package io.github.riken127.graphite.neo4j;

/** Work performed inside a retryable managed Neo4j transaction. */
@FunctionalInterface
public interface TransactionWork<T> {

  T execute(GraphiteOperations operations);
}
