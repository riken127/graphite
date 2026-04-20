package io.github.riken127.graphite.core;

/** Base contract for query predicates. */
public sealed interface Predicate permits EqualsPredicate {

  /**
   * Alias referenced by the predicate.
   *
   * @return alias name
   */
  String alias();

  /**
   * Property referenced by the predicate.
   *
   * @return property name
   */
  String property();
}
