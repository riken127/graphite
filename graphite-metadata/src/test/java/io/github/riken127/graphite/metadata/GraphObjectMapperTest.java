package io.github.riken127.graphite.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphObjectMapperTest {

  private final ReflectionNodeMetadataRegistry registry = new ReflectionNodeMetadataRegistry();

  @Test
  void mapsConstructorBackedImmutableClasses() {
    GraphObjectMapper mapper = new GraphObjectMapper(registry);

    ImmutableConsultant consultant =
        mapper.map(
            ImmutableConsultant.class,
            Map.of("id", "42", "display_name", "Ada", "scores", List.of(7L, 9L)));

    assertEquals("42", consultant.id());
    assertEquals("Ada", consultant.name());
    assertEquals(List.of(7, 9), consultant.scores());
    assertEquals(
        "display_name",
        registry.metadata(ImmutableConsultant.class).properties().get(1).graphName());
  }

  @Test
  void selectsAnnotatedConstructorAndSupportsCustomFactories() {
    GraphObjectFactories factories =
        GraphObjectFactories.builder()
            .add(
                FactoryConstructed.class,
                arguments ->
                    new FactoryConstructed((String) arguments[0], (Integer) arguments[1], true))
            .build();
    GraphObjectMapper mapper =
        new GraphObjectMapper(registry, GraphValueConverters.empty(), factories);

    FactoryConstructed result =
        mapper.map(FactoryConstructed.class, Map.of("name", "Ada", "rating", 8L));

    assertEquals("Ada", result.name());
    assertEquals(8, result.rating());
    assertEquals(true, result.createdByFactory());
  }

  @Test
  void requiresAnUnambiguousConstructionStrategy() {
    assertThrows(MetadataException.class, () -> registry.metadata(Ambiguous.class));
  }

  @Test
  void mapsAnnotatedConstructorParametersWithoutDependingOnFieldOrder() {
    GraphObjectMapper mapper = new GraphObjectMapper(registry);

    ReorderedNames names =
        mapper.map(ReorderedNames.class, Map.of("first_name", "Ada", "last_name", "Lovelace"));

    assertEquals("Ada", names.firstName());
    assertEquals("Lovelace", names.lastName());
  }

  @Test
  void rejectsAmbiguousParametersWhenNamesAndMappingAnnotationsAreAbsent() {
    MetadataException failure =
        assertThrows(
            MetadataException.class, () -> registry.metadata(UnnamedSameTypeParameters.class));

    assertEquals(true, failure.getMessage().contains("cannot be matched unambiguously"));
    assertEquals(true, failure.getMessage().contains("-parameters"));
  }

  @GraphNode("Consultant")
  private static final class ImmutableConsultant {

    @GraphId private final String id;

    @GraphProperty("display_name")
    private final String name;

    private final List<Integer> scores;

    private ImmutableConsultant(
        @GraphId String id, @GraphProperty("display_name") String name, List<Integer> scores) {
      this.id = id;
      this.name = name;
      this.scores = scores;
    }

    String id() {
      return id;
    }

    String name() {
      return name;
    }

    List<Integer> scores() {
      return scores;
    }
  }

  private static final class ReorderedNames {

    @GraphProperty("first_name")
    private final String firstName;

    @GraphProperty("last_name")
    private final String lastName;

    private ReorderedNames(
        @GraphProperty("last_name") String lastName,
        @GraphProperty("first_name") String firstName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    String firstName() {
      return firstName;
    }

    String lastName() {
      return lastName;
    }
  }

  private static final class UnnamedSameTypeParameters {

    private final String first;
    private final String second;

    private UnnamedSameTypeParameters(String first, String second) {
      this.first = first;
      this.second = second;
    }
  }

  private static final class FactoryConstructed {

    private final String name;
    private final int rating;
    private final boolean createdByFactory;

    @GraphConstructor
    private FactoryConstructed(String name, int rating) {
      this(name, rating, false);
    }

    private FactoryConstructed(String name, int rating, boolean createdByFactory) {
      this.name = name;
      this.rating = rating;
      this.createdByFactory = createdByFactory;
    }

    String name() {
      return name;
    }

    int rating() {
      return rating;
    }

    boolean createdByFactory() {
      return createdByFactory;
    }
  }

  private static final class Ambiguous {

    private final String value;

    private Ambiguous(String value) {
      this.value = value;
    }

    private Ambiguous(String value, int ignored) {
      this.value = value;
    }
  }
}
