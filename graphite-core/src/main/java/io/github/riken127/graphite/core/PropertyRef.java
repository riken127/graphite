package io.github.riken127.graphite.core;

/** Typed reference to a node property used in predicates. */
public final class PropertyRef {

  private final String alias;
  private final String name;

  PropertyRef(String alias, String name) {
    this.alias = AstValidator.requireAlias(alias);
    this.name = AstValidator.requireProperty(name);
  }

  /**
   * Builds an equality predicate for the property.
   *
   * @param value expected value
   * @return equality predicate
   */
  public Predicate eq(Object value) {
    return new EqualsPredicate(alias, name, value);
  }
}
