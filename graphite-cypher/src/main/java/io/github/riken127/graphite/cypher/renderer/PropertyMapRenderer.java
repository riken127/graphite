package io.github.riken127.graphite.cypher.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class PropertyMapRenderer {

  private PropertyMapRenderer() {}

  static String renderInlineMap(Map<String, Object> properties, ParameterAccumulator parameters) {
    List<String> parts = new ArrayList<>();

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      parts.add(entry.getKey() + ": " + parameters.add(entry.getValue()));
    }

    return String.join(", ", parts);
  }

  static String renderSetStatements(
      String alias, Map<String, Object> properties, ParameterAccumulator parameters) {
    List<String> parts = new ArrayList<>();

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      parts.add(alias + "." + entry.getKey() + " = " + parameters.add(entry.getValue()));
    }

    return String.join(", ", parts);
  }
}
