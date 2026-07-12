package io.github.riken127.graphite.neo4j.exception;

import java.util.Objects;

/** Neo4j-reported query or database failure. */
public class GraphiteDatabaseException extends GraphiteException {

  private final String code;
  private final String gqlStatus;

  /** Creates a translated Neo4j database exception. */
  public GraphiteDatabaseException(String message, String code, String gqlStatus, Throwable cause) {
    super(message, cause);
    this.code = Objects.requireNonNullElse(code, "");
    this.gqlStatus = Objects.requireNonNullElse(gqlStatus, "");
  }

  public String code() {
    return code;
  }

  public String gqlStatus() {
    return gqlStatus;
  }

  /** Whether retrying the complete transaction may succeed. */
  public boolean retryable() {
    return false;
  }
}
