package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.metadata.GraphObjectMapper;
import io.github.riken127.graphite.metadata.RecordEntityMapper;
import java.util.Objects;

/** Ready-made record mappers for Neo4j graph values. */
public final class Neo4jMappers {

  private Neo4jMappers() {}

  /** Maps a returned Neo4j node into an annotated Java record. */
  public static <T> Neo4jRecordMapper<T> node(
      String resultKey, Class<T> targetType, RecordEntityMapper entityMapper) {
    if (resultKey == null || resultKey.isBlank()) {
      throw new IllegalArgumentException("resultKey must not be blank");
    }
    Objects.requireNonNull(targetType, "targetType must not be null");
    Objects.requireNonNull(entityMapper, "entityMapper must not be null");
    return record -> entityMapper.map(targetType, record.value(resultKey).asNode().asMap());
  }

  /** Maps a returned Neo4j node into a constructor-backed immutable object. */
  public static <T> Neo4jRecordMapper<T> node(
      String resultKey, Class<T> targetType, GraphObjectMapper objectMapper) {
    if (resultKey == null || resultKey.isBlank()) {
      throw new IllegalArgumentException("resultKey must not be blank");
    }
    Objects.requireNonNull(targetType, "targetType must not be null");
    Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    return record -> objectMapper.map(targetType, record.value(resultKey).asNode().asMap());
  }

  /** Maps a returned Neo4j relationship's properties into an annotated Java record. */
  public static <T> Neo4jRecordMapper<T> relationship(
      String resultKey, Class<T> targetType, RecordEntityMapper entityMapper) {
    if (resultKey == null || resultKey.isBlank()) {
      throw new IllegalArgumentException("resultKey must not be blank");
    }
    Objects.requireNonNull(targetType, "targetType must not be null");
    Objects.requireNonNull(entityMapper, "entityMapper must not be null");
    return record -> entityMapper.map(targetType, record.value(resultKey).asRelationship().asMap());
  }

  /** Maps a returned Neo4j relationship into a constructor-backed immutable object. */
  public static <T> Neo4jRecordMapper<T> relationship(
      String resultKey, Class<T> targetType, GraphObjectMapper objectMapper) {
    if (resultKey == null || resultKey.isBlank()) {
      throw new IllegalArgumentException("resultKey must not be blank");
    }
    Objects.requireNonNull(targetType, "targetType must not be null");
    Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    return record -> objectMapper.map(targetType, record.value(resultKey).asRelationship().asMap());
  }
}
