package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;

/** Immutable node pattern for graph clauses. */
public record NodePattern(String label, String alias) {

  /**
   * Creates a validated node pattern.
   *
   * @param label node label
   * @param alias node alias
   */
  public NodePattern {
    label = AstValidator.requireLabel(label);
    alias = AstValidator.requireAlias(alias);
  }

  /**
   * Returns a new node pattern with a different alias.
   *
   * @param newAlias alias to use
   * @return updated node pattern
   */
  public NodePattern as(String newAlias) {
    return new NodePattern(label, newAlias);
  }
}
