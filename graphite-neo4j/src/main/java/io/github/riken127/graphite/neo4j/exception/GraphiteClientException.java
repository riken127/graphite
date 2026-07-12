package io.github.riken127.graphite.neo4j.exception;

/** Invalid query, parameters, authorization, or other client-originated failure. */
public final class GraphiteClientException extends GraphiteDatabaseException {

  public GraphiteClientException(String message, String code, String gqlStatus, Throwable cause) {
    super(message, code, gqlStatus, cause);
  }
}
