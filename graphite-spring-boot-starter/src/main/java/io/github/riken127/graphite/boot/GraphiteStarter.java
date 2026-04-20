package io.github.riken127.graphite.boot;

/** Marker API for the Graphite Spring Boot starter module. */
public final class GraphiteStarter {

  private GraphiteStarter() {}

  /**
   * Returns a user-facing module description.
   *
   * @return module description
   */
  public static String description() {
    return "Graphite Spring Boot starter";
  }
}
