package io.github.riken127.graphite.core.dsl;

import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.RelationshipDirection;
import io.github.riken127.graphite.core.model.RelationshipPattern;
import java.util.Objects;

/** Intermediate traversal builder completed by selecting a target node. */
public final class RelationshipStepBuilder {

  private final MatchBuilder owner;
  private final String type;
  private final RelationshipDirection direction;
  private String alias;
  private Integer minimumHops;
  private Integer maximumHops;

  RelationshipStepBuilder(MatchBuilder owner, String type, RelationshipDirection direction) {
    this.owner = Objects.requireNonNull(owner, "owner must not be null");
    this.type = Objects.requireNonNull(type, "type must not be null");
    this.direction = Objects.requireNonNull(direction, "direction must not be null");
  }

  /** Gives the relationship a query alias. */
  public RelationshipStepBuilder as(String relationshipAlias) {
    this.alias = relationshipAlias;
    return this;
  }

  /** Configures a variable-length relationship range. */
  public RelationshipStepBuilder hops(int minimum, int maximum) {
    this.minimumHops = minimum;
    this.maximumHops = maximum;
    return this;
  }

  /** Configures an exact relationship hop count. */
  public RelationshipStepBuilder hops(int count) {
    this.minimumHops = count;
    this.maximumHops = count;
    return this;
  }

  /** Configures an unbounded variable-length relationship range. */
  public RelationshipStepBuilder hopsFrom(int minimum) {
    this.minimumHops = minimum;
    this.maximumHops = null;
    return this;
  }

  /** Completes the traversal with its target node. */
  public MatchBuilder to(NodePattern target) {
    return owner.addTraversal(
        new RelationshipPattern(type, alias, direction, minimumHops, maximumHops), target);
  }
}
