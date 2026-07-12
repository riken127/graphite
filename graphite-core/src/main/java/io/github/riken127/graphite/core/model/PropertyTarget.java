package io.github.riken127.graphite.core.model;

import io.github.riken127.graphite.core.validation.AstValidator;

/** Alias-qualified property targeted by a write operation. */
public record PropertyTarget(String alias, String property) {

  /** Creates a validated property target. */
  public PropertyTarget {
    alias = AstValidator.requireAlias(alias);
    property = AstValidator.requireProperty(property);
  }
}
