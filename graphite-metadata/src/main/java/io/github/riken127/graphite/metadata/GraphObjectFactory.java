package io.github.riken127.graphite.metadata;

/** Creates one mapped object from converted constructor arguments in metadata order. */
@FunctionalInterface
public interface GraphObjectFactory<T> {

  /** Creates an object from a defensive copy of converted constructor arguments. */
  T create(Object[] arguments);
}
