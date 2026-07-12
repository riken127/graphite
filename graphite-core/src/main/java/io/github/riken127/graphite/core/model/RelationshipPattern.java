package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;

/** Immutable relationship pattern used between two nodes in a path. */
public record RelationshipPattern(
    String type,
    String alias,
    RelationshipDirection direction,
    Integer minimumHops,
    Integer maximumHops) {

  /** Creates a validated relationship pattern. */
  public RelationshipPattern {
    type = AstValidator.requireRelationshipType(type);
    alias = alias == null ? null : AstValidator.requireAlias(alias);
    if (direction == null) {
      throw new IllegalArgumentException("direction must not be null");
    }
    if (minimumHops != null && minimumHops < 0) {
      throw new IllegalArgumentException("minimumHops must be >= 0");
    }
    if (maximumHops != null && maximumHops < 0) {
      throw new IllegalArgumentException("maximumHops must be >= 0");
    }
    if (minimumHops == null && maximumHops != null) {
      throw new IllegalArgumentException("minimumHops is required when maximumHops is set");
    }
    if (minimumHops != null && maximumHops != null && maximumHops < minimumHops) {
      throw new IllegalArgumentException("maximumHops must be >= minimumHops");
    }
  }

  /** Returns whether this pattern has a variable-length hop range. */
  public boolean variableLength() {
    return minimumHops != null;
  }
}
