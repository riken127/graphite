package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.Sort;
import java.util.ArrayList;
import java.util.List;

final class RendererSupport {

  private RendererSupport() {}

  static String renderSorts(List<Sort> sorts) {
    List<String> parts = new ArrayList<>();

    for (Sort sort : sorts) {
      parts.add(sort.alias() + "." + sort.property() + " " + sort.direction().name());
    }

    return String.join(", ", parts);
  }
}
