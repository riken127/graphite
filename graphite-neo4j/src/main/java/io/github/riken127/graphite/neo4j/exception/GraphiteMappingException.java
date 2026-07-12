package io.github.riken127.graphite.neo4j.exception;

/** Application record-mapping failure. */
public final class GraphiteMappingException extends GraphiteException {

  public GraphiteMappingException(String message, Throwable cause) {
    super(message, cause);
  }
}
