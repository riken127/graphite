package io.github.riken127.graphite.processor;

import io.github.riken127.graphite.metadata.GraphConstructor;
import io.github.riken127.graphite.metadata.GraphId;
import io.github.riken127.graphite.metadata.GraphNode;
import io.github.riken127.graphite.metadata.GraphProperty;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/** Generates typed Graphite metamodel classes for {@link GraphNode} types. */
public final class GraphMetamodelProcessor extends AbstractProcessor {

  private static final String ANNOTATION = GraphNode.class.getCanonicalName();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(ANNOTATION);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_21;
  }

  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
    for (Element element : roundEnvironment.getElementsAnnotatedWith(GraphNode.class)) {
      if (element instanceof TypeElement type) {
        generate(type);
      } else {
        error(element, "@GraphNode is only supported on classes and records");
      }
    }
    return false;
  }

  private void generate(TypeElement type) {
    if (type.getModifiers().contains(Modifier.PRIVATE)) {
      error(type, "cannot generate a metamodel for a private type");
      return;
    }
    GraphNode graphNode = type.getAnnotation(GraphNode.class);
    if (!validIdentifier(graphNode.value())) {
      error(type, "invalid graph label: " + graphNode.value());
      return;
    }
    List<Property> properties = properties(type);
    if (properties == null || properties.isEmpty()) {
      return;
    }
    if (!validate(type, properties)) {
      return;
    }

    PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(type);
    String packageName = packageElement.getQualifiedName().toString();
    String generatedName = generatedSimpleName(type);
    String qualifiedName =
        packageName.isEmpty() ? generatedName : packageName + "." + generatedName;
    try {
      JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qualifiedName, type);
      try (Writer writer = sourceFile.openWriter()) {
        writer.write(source(packageName, generatedName, type, graphNode.value(), properties));
      }
    } catch (IOException failure) {
      error(type, "failed to generate metamodel: " + failure.getMessage());
    }
  }

  private List<Property> properties(TypeElement type) {
    if (type.getKind() == ElementKind.RECORD) {
      List<Property> properties = new ArrayList<>();
      int index = 0;
      for (RecordComponentElement component : type.getRecordComponents()) {
        properties.add(
            property(component, field(type, component.getSimpleName().toString()), index));
        index++;
      }
      return properties;
    }

    List<ExecutableElement> constructors = ElementFilter.constructorsIn(type.getEnclosedElements());
    List<ExecutableElement> selected =
        constructors.stream()
            .filter(constructor -> constructor.getAnnotation(GraphConstructor.class) != null)
            .toList();
    ExecutableElement constructor;
    if (selected.size() > 1) {
      error(type, "multiple @GraphConstructor declarations");
      return null;
    } else if (selected.size() == 1) {
      constructor = selected.getFirst();
    } else if (constructors.size() == 1) {
      constructor = constructors.getFirst();
    } else {
      error(type, "declare one constructor or select one with @GraphConstructor");
      return null;
    }
    if (constructor.getParameters().isEmpty()) {
      error(type, "mapped constructor must declare at least one property");
      return null;
    }

    List<Property> properties = new ArrayList<>();
    int index = 0;
    for (VariableElement parameter : constructor.getParameters()) {
      VariableElement field = field(type, parameter.getSimpleName().toString());
      if (field == null) {
        error(parameter, "constructor parameter must have a matching field");
        return null;
      }
      properties.add(property(parameter, field, index));
      index++;
    }
    return properties;
  }

  private Property property(Element primary, VariableElement field, int index) {
    GraphProperty property = primary.getAnnotation(GraphProperty.class);
    if (property == null && field != null) {
      property = field.getAnnotation(GraphProperty.class);
    }
    boolean id = primary.getAnnotation(GraphId.class) != null;
    if (!id && field != null) {
      id = field.getAnnotation(GraphId.class) != null;
    }
    String javaName = primary.getSimpleName().toString();
    String graphName = property == null ? javaName : property.value();
    TypeMirror type = primary.asType();
    return new Property(javaName, graphName, typeName(type), classLiteral(type), id, index);
  }

  private boolean validate(TypeElement type, List<Property> properties) {
    Set<String> graphNames = new HashSet<>();
    Set<String> constants = new HashSet<>();
    boolean foundId = false;
    for (Property property : properties) {
      if (!validIdentifier(property.javaName()) || !validIdentifier(property.graphName())) {
        error(type, "invalid mapped property: " + property.graphName());
        return false;
      }
      if (!graphNames.add(property.graphName())) {
        error(type, "duplicate graph property: " + property.graphName());
        return false;
      }
      if (!constants.add(constantName(property.javaName()))) {
        error(type, "duplicate generated property constant: " + property.javaName());
        return false;
      }
      if (property.id() && foundId) {
        error(type, "multiple @GraphId properties");
        return false;
      }
      foundId |= property.id();
    }
    return true;
  }

  private String source(
      String packageName,
      String generatedName,
      TypeElement type,
      String label,
      List<Property> properties) {
    String owner = type.getQualifiedName().toString();
    StringBuilder source = new StringBuilder();
    if (!packageName.isEmpty()) {
      source.append("package ").append(packageName).append(";\n\n");
    }
    source
        .append("@")
        .append(Generated.class.getCanonicalName())
        .append("(\"")
        .append(GraphMetamodelProcessor.class.getCanonicalName())
        .append("\")\n")
        .append("public final class ")
        .append(generatedName)
        .append(" {\n\n")
        .append("  public static final String LABEL = \"")
        .append(escape(label))
        .append("\";\n\n");
    for (Property property : properties) {
      source
          .append("  public static final io.github.riken127.graphite.metadata.GraphAttribute<")
          .append(owner)
          .append(", ")
          .append(property.typeName())
          .append("> ")
          .append(constantName(property.javaName()))
          .append(" =\n      io.github.riken127.graphite.metadata.GraphAttribute.of(")
          .append(owner)
          .append(".class, \"")
          .append(escape(property.javaName()))
          .append("\", \"")
          .append(escape(property.graphName()))
          .append("\", ")
          .append(property.classLiteral())
          .append(", ")
          .append(property.id())
          .append(", ")
          .append(property.index())
          .append(");\n\n");
    }
    source
        .append("  public static final io.github.riken127.graphite.metadata.GraphMetamodel<")
        .append(owner)
        .append("> TYPE =\n      io.github.riken127.graphite.metadata.GraphMetamodel.of(")
        .append(owner)
        .append(".class, LABEL");
    for (Property property : properties) {
      source.append(", ").append(constantName(property.javaName()));
    }
    source
        .append(");\n\n")
        .append("  private ")
        .append(generatedName)
        .append("() {}\n\n")
        .append("  public static io.github.riken127.graphite.metadata.GraphEntity<")
        .append(owner)
        .append("> entity(String alias) {\n")
        .append("    return TYPE.entity(alias);\n")
        .append("  }\n")
        .append("}\n");
    return source.toString();
  }

  private String typeName(TypeMirror type) {
    TypeMirror erased = processingEnv.getTypeUtils().erasure(type);
    if (erased.getKind().isPrimitive()) {
      return processingEnv
          .getTypeUtils()
          .boxedClass((javax.lang.model.type.PrimitiveType) erased)
          .getQualifiedName()
          .toString();
    }
    if (erased.getKind() == TypeKind.DECLARED || erased.getKind() == TypeKind.ARRAY) {
      return erased.toString();
    }
    return Object.class.getCanonicalName();
  }

  private String classLiteral(TypeMirror type) {
    return typeName(type) + ".class";
  }

  private static VariableElement field(TypeElement type, String name) {
    for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
      if (!field.getModifiers().contains(Modifier.STATIC)
          && field.getSimpleName().contentEquals(name)) {
        return field;
      }
    }
    return null;
  }

  private static String generatedSimpleName(TypeElement type) {
    List<String> names = new ArrayList<>();
    Element current = type;
    while (current instanceof TypeElement currentType) {
      names.add(0, currentType.getSimpleName().toString());
      current = current.getEnclosingElement();
    }
    return String.join("_", names) + "_";
  }

  private static String constantName(String value) {
    StringBuilder result = new StringBuilder();
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (index > 0 && Character.isUpperCase(current)) {
        result.append('_');
      }
      result.append(Character.toUpperCase(current));
    }
    return result.toString().replaceAll("[^A-Z0-9_]", "_");
  }

  private static boolean validIdentifier(String value) {
    return value != null && value.matches("[A-Za-z_][A-Za-z0-9_]*");
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private void error(Element element, String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }

  private record Property(
      String javaName,
      String graphName,
      String typeName,
      String classLiteral,
      boolean id,
      int index) {}
}
