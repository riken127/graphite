package io.github.riken127.graphite.neo4j.exception;

/** Failure that may succeed when the complete transaction is retried. */
public final class GraphiteTransientException extends GraphiteDatabaseException {

  public GraphiteTransientException(
      String message, String code, String gqlStatus, Throwable cause) {
    super(message, code, gqlStatus, cause);
  }

  @Override
  public boolean retryable() {
    return true;
  }
}
