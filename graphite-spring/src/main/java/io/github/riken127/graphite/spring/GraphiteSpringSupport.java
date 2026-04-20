package io.github.riken127.graphite.spring;

/** Convenience helpers for Spring-oriented Graphite modules. */
public final class GraphiteSpringSupport {

  private GraphiteSpringSupport() {}

  /**
   * Generates a deterministic bean name for a domain type.
   *
   * @param domainType domain class
   * @return bean name
   */
  public static String beanNameFor(Class<?> domainType) {
    if (domainType == null) {
      throw new IllegalArgumentException("domainType must not be null");
    }
    String simpleName = domainType.getSimpleName();
    if (simpleName.isEmpty()) {
      return "graphiteBean";
    }
    return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
  }
}
