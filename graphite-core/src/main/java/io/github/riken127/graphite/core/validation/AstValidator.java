package io.github.riken127.graphite.core.validation;

import java.util.regex.Pattern;

/** Shared validation helpers for query model values. */
public final class AstValidator {

  private static final Pattern GRAPH_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
  private static final Pattern PROJECTION =
      Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)?");

  private AstValidator() {}

  /**
   * Validates an alias identifier.
   *
   * @param alias alias value
   * @return validated alias
   */
  public static String requireAlias(String alias) {
    return requireIdentifier(alias, "alias");
  }

  /**
   * Validates a property identifier.
   *
   * @param property property value
   * @return validated property
   */
  public static String requireProperty(String property) {
    return requireIdentifier(property, "property");
  }

  /**
   * Validates a label identifier.
   *
   * @param label label value
   * @return validated label
   */
  public static String requireLabel(String label) {
    return requireIdentifier(label, "label");
  }

  /**
   * Validates a projection expression.
   *
   * @param projection projection value
   * @return validated projection
   */
  public static String requireProjection(String projection) {
    if (projection == null || projection.isBlank()) {
      throw new IllegalArgumentException("projection must not be blank");
    }

    String trimmed = projection.trim();
    if (!PROJECTION.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("projection must be alias or alias.property");
    }
    return trimmed;
  }

  /**
   * Extracts the alias part from a projection.
   *
   * @param projection projection value
   * @return alias part of projection
   */
  public static String projectionAlias(String projection) {
    String validatedProjection = requireProjection(projection);
    int dotIndex = validatedProjection.indexOf('.');
    return dotIndex < 0 ? validatedProjection : validatedProjection.substring(0, dotIndex);
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
