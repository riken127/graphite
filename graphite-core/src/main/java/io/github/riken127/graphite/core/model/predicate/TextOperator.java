package io.github.riken127.graphite.core.model.predicate;

/** Text predicate operators supported by Cypher. */
public enum TextOperator {
  STARTS_WITH("STARTS WITH"),
  ENDS_WITH("ENDS WITH"),
  CONTAINS("CONTAINS");

  private final String cypherKeyword;

  TextOperator(String cypherKeyword) {
    this.cypherKeyword = cypherKeyword;
  }

  /**
   * Returns the Cypher keyword for the operator.
   *
   * @return Cypher text operator keyword
   */
  public String cypherKeyword() {
    return cypherKeyword;
  }
}
