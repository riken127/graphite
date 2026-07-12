package io.github.riken127.graphite.metadata;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable registry of application-defined graph object factories. */
public final class GraphObjectFactories {

  private static final GraphObjectFactories EMPTY = new GraphObjectFactories(Map.of());

  private final Map<Class<?>, GraphObjectFactory<?>> factories;

  private GraphObjectFactories(Map<Class<?>, GraphObjectFactory<?>> factories) {
    this.factories = Map.copyOf(factories);
  }

  /** Returns an empty factory registry that uses reflection for every mapped type. */
  public static GraphObjectFactories empty() {
    return EMPTY;
  }

  /** Starts a custom factory registry. */
  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  <T> Optional<GraphObjectFactory<T>> factory(Class<T> javaType) {
    return Optional.ofNullable((GraphObjectFactory<T>) factories.get(javaType));
  }

  /** Builder for immutable object factory registries. */
  public static final class Builder {

    private final Map<Class<?>, GraphObjectFactory<?>> factories = new LinkedHashMap<>();

    private Builder() {}

    /** Registers one exact mapped type. */
    public <T> Builder add(Class<T> javaType, GraphObjectFactory<T> factory) {
      Objects.requireNonNull(javaType, "javaType must not be null");
      Objects.requireNonNull(factory, "factory must not be null");
      if (factories.putIfAbsent(javaType, factory) != null) {
        throw new IllegalArgumentException("factory already registered for " + javaType.getName());
      }
      return this;
    }

    public GraphObjectFactories build() {
      return factories.isEmpty() ? EMPTY : new GraphObjectFactories(factories);
    }
  }
}
