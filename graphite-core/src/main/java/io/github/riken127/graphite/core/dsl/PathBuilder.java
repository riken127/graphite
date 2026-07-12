package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.RelationshipDirection;
import io.github.riken127.graphite.core.model.RelationshipPattern;
import io.github.riken127.graphite.core.model.Traversal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builder for reusable path patterns outside a fixed MATCH query shape. */
public final class PathBuilder {

  private final NodePattern start;
  private final List<Traversal> traversals = new ArrayList<>();

  PathBuilder(NodePattern start) {
    this.start = Objects.requireNonNull(start, "start must not be null");
  }

  /** Adds an outgoing single-hop relationship. */
  public PathBuilder out(String type, NodePattern target) {
    return singleHop(type, RelationshipDirection.OUTGOING, target);
  }

  /** Adds an incoming single-hop relationship. */
  public PathBuilder in(String type, NodePattern target) {
    return singleHop(type, RelationshipDirection.INCOMING, target);
  }

  /** Adds an undirected single-hop relationship. */
  public PathBuilder related(String type, NodePattern target) {
    return singleHop(type, RelationshipDirection.UNDIRECTED, target);
  }

  /** Adds a fully configured relationship pattern. */
  public PathBuilder relationship(RelationshipPattern relationship, NodePattern target) {
    traversals.add(new Traversal(relationship, target));
    return this;
  }

  /** Builds an immutable reusable path pattern. */
  public PathPattern build() {
    return new PathPattern(start, traversals);
  }

  private PathBuilder singleHop(String type, RelationshipDirection direction, NodePattern target) {
    return relationship(new RelationshipPattern(type, null, direction, null, null), target);
  }
}
