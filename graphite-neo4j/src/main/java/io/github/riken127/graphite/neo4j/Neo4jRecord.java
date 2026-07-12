package io.github.riken127.graphite.neo4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

/** Read-only view over a Neo4j result record. */
public final class Neo4jRecord {

  private final Record delegate;

  Neo4jRecord(Record delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  public List<String> keys() {
    return List.copyOf(delegate.keys());
  }

  public boolean contains(String key) {
    return delegate.containsKey(key);
  }

  public Value value(String key) {
    return delegate.get(Objects.requireNonNull(key, "key must not be null"));
  }

  public Object get(String key) {
    return value(key).asObject();
  }

  public <T> T as(Class<T> targetType) {
    return delegate.as(Objects.requireNonNull(targetType, "targetType must not be null"));
  }

  public Map<String, Object> asMap() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(delegate.asMap()));
  }

  /** Returns the underlying driver record for advanced Neo4j-specific mapping. */
  public Record driverRecord() {
    return delegate;
  }
}
