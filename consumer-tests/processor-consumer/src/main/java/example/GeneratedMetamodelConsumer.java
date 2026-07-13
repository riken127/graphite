package example;

import io.github.riken127.graphite.metadata.GraphEntity;

/** References generated code in the same compilation round. */
public final class GeneratedMetamodelConsumer {

  private GeneratedMetamodelConsumer() {}

  public static GraphEntity<Consultant> entity() {
    return Consultant_.entity("consultant");
  }
}
