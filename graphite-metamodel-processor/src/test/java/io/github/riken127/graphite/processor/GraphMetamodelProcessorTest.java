package io.github.riken127.graphite.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.metadata.GraphAttribute;
import io.github.riken127.graphite.metadata.GraphEntity;
import io.github.riken127.graphite.metadata.GraphMetamodel;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphMetamodelProcessorTest {

  @Test
  void generatesAndCompilesTypedMetamodel(@TempDir Path directory) throws Exception {
    Path sourceRoot = Files.createDirectories(directory.resolve("source/example"));
    Path generatedRoot = Files.createDirectories(directory.resolve("generated"));
    Path classesRoot = Files.createDirectories(directory.resolve("classes"));
    Path source = sourceRoot.resolve("Consultant.java");
    Files.writeString(
        source,
        """
        package example;

        import io.github.riken127.graphite.metadata.GraphId;
        import io.github.riken127.graphite.metadata.GraphNode;
        import io.github.riken127.graphite.metadata.GraphProperty;

        @GraphNode("ConsultantNode")
        public record Consultant(
            @GraphId String id,
            @GraphProperty("display_name") String name,
            int rating) {}
        """);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler);
    try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null)) {
      JavaCompiler.CompilationTask task =
          compiler.getTask(
              null,
              files,
              null,
              List.of(
                  "--release",
                  "21",
                  "-classpath",
                  System.getProperty("java.class.path"),
                  "-d",
                  classesRoot.toString(),
                  "-s",
                  generatedRoot.toString()),
              null,
              files.getJavaFileObjects(source));
      task.setProcessors(List.of(new GraphMetamodelProcessor()));
      assertTrue(task.call());
    }

    Path generated = generatedRoot.resolve("example/Consultant_.java");
    assertTrue(Files.exists(generated));
    String generatedSource = Files.readString(generated);
    assertTrue(
        generatedSource.contains("GraphAttribute<example.Consultant, java.lang.String> NAME"));
    assertTrue(generatedSource.contains("\"display_name\""));

    try (URLClassLoader loader =
        new URLClassLoader(
            new java.net.URL[] {classesRoot.toUri().toURL()}, getClass().getClassLoader())) {
      Class<?> generatedType = loader.loadClass("example.Consultant_");
      GraphMetamodel<?> metamodel = (GraphMetamodel<?>) generatedType.getField("TYPE").get(null);
      GraphAttribute<?, ?> name = (GraphAttribute<?, ?>) generatedType.getField("NAME").get(null);

      assertEquals("ConsultantNode", metamodel.label());
      assertEquals("display_name", name.graphName());
      GraphEntity<?> entity = metamodel.entity("c");
      assertEquals("c", entity.alias());
      assertEquals("display_name", property(name, entity).property());
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static TypedPropertyRef<?> property(
      GraphAttribute<?, ?> attribute, GraphEntity<?> entity) {
    return ((GraphAttribute) attribute).of((GraphEntity) entity);
  }
}
