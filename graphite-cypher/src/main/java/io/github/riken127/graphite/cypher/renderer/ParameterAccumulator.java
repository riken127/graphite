package io.github.riken127.graphite.cypher.renderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class ParameterAccumulator {

  private final Map<String, Object> parameters = new LinkedHashMap<>();
  private int index;

  String add(Object value) {
    Objects.requireNonNull(value, "value must not be null");
    String parameterName = "p" + index++;
    parameters.put(parameterName, value);
    return "$" + parameterName;
  }

  Map<String, Object> parameters() {
    return parameters;
  }
}
