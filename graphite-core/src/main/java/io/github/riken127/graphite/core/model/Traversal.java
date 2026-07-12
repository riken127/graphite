package io.github.riken127.graphite.core.model;

import java.util.Objects;

/** A relationship and its target node in a path pattern. */
public record Traversal(RelationshipPattern relationship, NodePattern target) {

  /** Creates a validated traversal segment. */
  public Traversal {
    Objects.requireNonNull(relationship, "relationship must not be null");
    Objects.requireNonNull(target, "target must not be null");
  }
}
