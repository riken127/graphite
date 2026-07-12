package io.github.riken127.graphite.core.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Immutable node-and-relationship path pattern. */
public record PathPattern(NodePattern start, List<Traversal> traversals) {

  /** Creates a validated path pattern. */
  public PathPattern {
    Objects.requireNonNull(start, "start must not be null");
    traversals = List.copyOf(Objects.requireNonNull(traversals, "traversals must not be null"));

    Set<String> aliases = new LinkedHashSet<>();
    requireUniqueAlias(aliases, start.alias());
    for (Traversal traversal : traversals) {
      Objects.requireNonNull(traversal, "traversal must not be null");
      if (traversal.relationship().alias() != null) {
        requireUniqueAlias(aliases, traversal.relationship().alias());
      }
      requireUniqueAlias(aliases, traversal.target().alias());
    }
  }

  /** Returns every node and relationship alias in declaration order. */
  public Set<String> aliases() {
    Set<String> aliases = new LinkedHashSet<>(nodeAliases());
    aliases.addAll(relationshipAliases());
    return Collections.unmodifiableSet(aliases);
  }

  /** Returns every node alias in declaration order. */
  public Set<String> nodeAliases() {
    Set<String> aliases = new LinkedHashSet<>();
    aliases.add(start.alias());
    for (Traversal traversal : traversals) {
      aliases.add(traversal.target().alias());
    }
    return Collections.unmodifiableSet(aliases);
  }

  /** Returns every relationship alias in declaration order. */
  public Set<String> relationshipAliases() {
    Set<String> aliases = new LinkedHashSet<>();
    for (Traversal traversal : traversals) {
      if (traversal.relationship().alias() != null) {
        aliases.add(traversal.relationship().alias());
      }
    }
    return Collections.unmodifiableSet(aliases);
  }

  private static void requireUniqueAlias(Set<String> aliases, String alias) {
    if (!aliases.add(alias)) {
      throw new IllegalArgumentException("duplicate pattern alias: " + alias);
    }
  }
}
