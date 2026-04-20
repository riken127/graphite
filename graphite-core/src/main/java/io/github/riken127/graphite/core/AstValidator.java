package io.github.riken127.graphite.core;

import java.util.regex.Pattern;

final class AstValidator {

  private static final Pattern GRAPH_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  private AstValidator() {}

  static String requireAlias(String alias) {
    return requireIdentifier(alias, "alias");
  }

  static String requireProperty(String property) {
    return requireIdentifier(property, "property");
  }

  static String requireLabel(String label) {
    return requireIdentifier(label, "label");
  }

  private static String requireIdentifier(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }

    String trimmed = value.trim();
    if (!GRAPH_IDENTIFIER.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(fieldName + " must be a valid graph identifier");
    }
    return trimmed;
  }
}
