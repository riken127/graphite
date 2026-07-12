package io.github.riken127.graphite.neo4j.exception;

/** Base runtime exception for Graphite execution failures. */
public class GraphiteException extends RuntimeException {

  public GraphiteException(String message) {
    super(message);
  }

  public GraphiteException(String message, Throwable cause) {
    super(message, cause);
  }
}
