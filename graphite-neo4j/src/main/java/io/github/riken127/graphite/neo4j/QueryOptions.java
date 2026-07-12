package io.github.riken127.graphite.neo4j;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

/** Immutable database, routing, transaction, and causal-consistency options. */
public final class QueryOptions {

  private static final QueryOptions DEFAULTS = builder().build();

  private final String database;
  private final QueryAccessMode accessMode;
  private final Duration timeout;
  private final Map<String, Object> metadata;
  private final Long fetchSize;
  private final Set<String> bookmarks;
  private final String impersonatedUser;

  private QueryOptions(Builder builder) {
    this.database = builder.database;
    this.accessMode = builder.accessMode;
    this.timeout = builder.timeout;
    this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(builder.metadata));
    this.fetchSize = builder.fetchSize;
    this.bookmarks = Collections.unmodifiableSet(new LinkedHashSet<>(builder.bookmarks));
    this.impersonatedUser = builder.impersonatedUser;
  }

  /** Returns the default query options. */
  public static QueryOptions defaults() {
    return DEFAULTS;
  }

  /** Starts an options builder. */
  public static Builder builder() {
    return new Builder();
  }

  /** Starts a builder initialized from existing options. */
  public static Builder builder(QueryOptions source) {
    return new Builder(Objects.requireNonNull(source, "source must not be null"));
  }

  public Optional<String> database() {
    return Optional.ofNullable(database);
  }

  public QueryAccessMode accessMode() {
    return accessMode;
  }

  public Optional<Duration> timeout() {
    return Optional.ofNullable(timeout);
  }

  public Map<String, Object> metadata() {
    return metadata;
  }

  public OptionalLong fetchSize() {
    return fetchSize == null ? OptionalLong.empty() : OptionalLong.of(fetchSize);
  }

  public Set<String> bookmarks() {
    return bookmarks;
  }

  public Optional<String> impersonatedUser() {
    return Optional.ofNullable(impersonatedUser);
  }

  /** Builder for immutable query options. */
  public static final class Builder {

    private String database;
    private QueryAccessMode accessMode = QueryAccessMode.AUTO;
    private Duration timeout;
    private Map<String, Object> metadata = Map.of();
    private Long fetchSize;
    private Set<String> bookmarks = Set.of();
    private String impersonatedUser;

    private Builder() {}

    private Builder(QueryOptions source) {
      this.database = source.database;
      this.accessMode = source.accessMode;
      this.timeout = source.timeout;
      this.metadata = source.metadata;
      this.fetchSize = source.fetchSize;
      this.bookmarks = source.bookmarks;
      this.impersonatedUser = source.impersonatedUser;
    }

    public Builder database(String value) {
      this.database = requireText(value, "database");
      return this;
    }

    public Builder accessMode(QueryAccessMode value) {
      this.accessMode = Objects.requireNonNull(value, "accessMode must not be null");
      return this;
    }

    /** Sets the server-side transaction timeout. */
    public Builder timeout(Duration value) {
      Objects.requireNonNull(value, "timeout must not be null");
      if (value.isNegative() || value.isZero()) {
        throw new IllegalArgumentException("timeout must be positive");
      }
      this.timeout = value;
      return this;
    }

    /** Sets transaction metadata visible to Neo4j monitoring and query inspection. */
    public Builder metadata(Map<String, ?> value) {
      Objects.requireNonNull(value, "metadata must not be null");
      Map<String, Object> copied = new LinkedHashMap<>();
      for (Map.Entry<String, ?> entry : value.entrySet()) {
        copied.put(
            requireText(entry.getKey(), "metadata key"),
            Objects.requireNonNull(entry.getValue(), "metadata value must not be null"));
      }
      this.metadata = copied;
      return this;
    }

    /** Sets the driver's result fetch size. */
    public Builder fetchSize(long value) {
      if (value <= 0) {
        throw new IllegalArgumentException("fetchSize must be positive");
      }
      this.fetchSize = value;
      return this;
    }

    /** Sets causal bookmarks that the session must observe. */
    public Builder bookmarks(Set<String> value) {
      Objects.requireNonNull(value, "bookmarks must not be null");
      Set<String> copied = new LinkedHashSet<>();
      for (String bookmark : value) {
        copied.add(requireText(bookmark, "bookmark"));
      }
      this.bookmarks = copied;
      return this;
    }

    public Builder impersonatedUser(String value) {
      this.impersonatedUser = requireText(value, "impersonatedUser");
      return this;
    }

    public QueryOptions build() {
      return new QueryOptions(this);
    }

    private static String requireText(String value, String fieldName) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(fieldName + " must not be blank");
      }
      return value.trim();
    }
  }
}
