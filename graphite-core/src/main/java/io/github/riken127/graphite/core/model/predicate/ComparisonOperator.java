package io.github.riken127.graphite.core.model.predicate;

/** Operators for binary comparison predicates. */
public enum ComparisonOperator {
  EQ("="),
  NE("<>"),
  GT(">"),
  GTE(">="),
  LT("<"),
  LTE("<=");

  private final String cypherSymbol;

  ComparisonOperator(String cypherSymbol) {
    this.cypherSymbol = cypherSymbol;
  }

  /**
   * Returns the Cypher symbol for the operator.
   *
   * @return Cypher comparison symbol
   */
  public String cypherSymbol() {
    return cypherSymbol;
  }
}
