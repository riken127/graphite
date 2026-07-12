package io.github.riken127.graphite.metadata;

import java.lang.reflect.Type;

/** Extension point for application-specific graph value conversion. */
public interface GraphValueConverter {

  /** Returns whether this converter handles the source value and requested target type. */
  boolean supports(Object source, Type targetType);

  /** Converts the source value to the requested target type. */
  Object convert(Object source, Type targetType);
}
