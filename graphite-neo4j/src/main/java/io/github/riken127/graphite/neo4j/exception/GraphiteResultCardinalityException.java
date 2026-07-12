package io.github.riken127.graphite.neo4j.exception;

/** Result count did not match a caller's single-result expectation. */
public final class GraphiteResultCardinalityException extends GraphiteException {

  private final String expected;
  private final int actual;

  /** Creates an exception describing the expected and actual record counts. */
  public GraphiteResultCardinalityException(String expected, int actual) {
    super("expected " + expected + " result record(s), but received " + actual);
    this.expected = expected;
    this.actual = actual;
  }

  public String expected() {
    return expected;
  }

  public int actual() {
    return actual;
  }
}
