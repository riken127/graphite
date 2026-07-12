package io.github.riken127.graphite.metadata;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable ordered registry of application graph value converters. */
public final class GraphValueConverters {

  private static final GraphValueConverters EMPTY = new GraphValueConverters(List.of());

  private final List<GraphValueConverter> converters;

  private GraphValueConverters(List<GraphValueConverter> converters) {
    this.converters = List.copyOf(converters);
  }

  /** Returns an empty converter registry. */
  public static GraphValueConverters empty() {
    return EMPTY;
  }

  /** Starts a custom converter registry builder. */
  public static Builder builder() {
    return new Builder();
  }

  Optional<Object> convert(Object source, Type targetType) {
    for (GraphValueConverter converter : converters) {
      if (converter.supports(source, targetType)) {
        return Optional.ofNullable(converter.convert(source, targetType));
      }
    }
    return Optional.empty();
  }

  /** Mutable builder for converter registration order. */
  public static final class Builder {

    private final List<GraphValueConverter> converters = new ArrayList<>();

    /** Adds a converter after previously registered converters. */
    public Builder add(GraphValueConverter converter) {
      converters.add(Objects.requireNonNull(converter, "converter must not be null"));
      return this;
    }

    /** Builds an immutable converter registry. */
    public GraphValueConverters build() {
      return new GraphValueConverters(converters);
    }
  }
}
