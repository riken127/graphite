package io.github.riken127.graphite.core.model.expression;

import io.github.riken127.graphite.core.validation.AstValidator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Map literal containing expression values in stable insertion order. */
public record MapExpression(Map<String, Expression<?>> entries)
    implements Expression<Map<String, Object>> {

  /** Creates a validated map expression. */
  public MapExpression {
    Objects.requireNonNull(entries, "entries must not be null");
    Map<String, Expression<?>> copy = new LinkedHashMap<>();
    entries.forEach(
        (key, value) ->
            copy.put(
                AstValidator.requireProperty(key),
                Objects.requireNonNull(value, "entry value must not be null")));
    entries = Collections.unmodifiableMap(copy);
  }

  /** Returns the erased map result type. */
  @Override
  @SuppressWarnings("unchecked")
  public Class<Map<String, Object>> valueType() {
    return (Class<Map<String, Object>>) (Class<?>) Map.class;
  }
}
