package io.github.riken127.graphite.core.model.expression;

/** Typed value-producing node in the query expression tree. */
public interface Expression<T> {

  /** Returns the Java value type produced by this expression. */
  Class<T> valueType();
}
