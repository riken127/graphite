package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.neo4j.exception.GraphiteClientException;
import io.github.riken127.graphite.neo4j.exception.GraphiteConnectivityException;
import io.github.riken127.graphite.neo4j.exception.GraphiteDatabaseException;
import io.github.riken127.graphite.neo4j.exception.GraphiteException;
import io.github.riken127.graphite.neo4j.exception.GraphiteTransientException;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.RetryableException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;

final class DriverExceptionTranslator {

  private DriverExceptionTranslator() {}

  static RuntimeException translate(RuntimeException failure) {
    if (failure instanceof GraphiteException) {
      return failure;
    }
    if (failure instanceof ServiceUnavailableException
        || failure instanceof SessionExpiredException) {
      return new GraphiteConnectivityException(failure.getMessage(), failure);
    }
    if (failure instanceof Neo4jException neo4jFailure) {
      if (failure instanceof RetryableException) {
        return new GraphiteTransientException(
            neo4jFailure.getMessage(), neo4jFailure.code(), neo4jFailure.gqlStatus(), neo4jFailure);
      }
      if (failure instanceof ClientException) {
        return new GraphiteClientException(
            neo4jFailure.getMessage(), neo4jFailure.code(), neo4jFailure.gqlStatus(), neo4jFailure);
      }
      return new GraphiteDatabaseException(
          neo4jFailure.getMessage(), neo4jFailure.code(), neo4jFailure.gqlStatus(), neo4jFailure);
    }
    return failure;
  }
}
