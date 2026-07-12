package io.github.riken127.graphite.neo4j.exception;

/** Connectivity, discovery, or expired-session failure. */
public final class GraphiteConnectivityException extends GraphiteException {

  public GraphiteConnectivityException(String message, Throwable cause) {
    super(message, cause);
  }
}
