package io.github.riken127.graphite.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecordEntityMapperTest {

  private final ReflectionNodeMetadataRegistry registry = new ReflectionNodeMetadataRegistry();
  private final RecordEntityMapper mapper = new RecordEntityMapper(registry);

  @Test
  void resolvesAndCachesAnnotatedRecordMetadata() {
    NodeMetadata first = registry.metadata(Consultant.class);
    NodeMetadata second = registry.metadata(Consultant.class);

    assertSame(first, second);
    assertEquals("Consultant", first.label());
    assertEquals("id", first.idProperty().orElseThrow().graphName());
    assertEquals("display_name", first.properties().get(1).graphName());
    assertEquals(1, registry.cachedTypeCount());
  }

  @Test
  void constructsRecordAndCoercesNeo4jScalarRepresentations() {
    Consultant consultant =
        mapper.map(
            Consultant.class,
            Map.of(
                "id", "42",
                "display_name", "Julia",
                "rating", 7L,
                "status", "ACTIVE"));

    assertEquals(new Consultant("42", "Julia", 7, Status.ACTIVE), consultant);
  }

  @Test
  void rejectsInvalidMetadataAndMissingPrimitiveValues() {
    assertThrows(MetadataException.class, () -> registry.metadata(NonRecordType.class));
    assertThrows(MetadataException.class, () -> registry.metadata(MultipleIds.class));
    assertThrows(
        MetadataMappingException.class,
        () ->
            mapper.map(
                Consultant.class, Map.of("id", "42", "display_name", "Julia", "status", "ACTIVE")));
  }

  @Test
  void createsMetadataBackedTypedEntityReferences() {
    GraphEntity<Consultant> consultant =
        new GraphEntityFactory(registry).entity(Consultant.class, "c");

    assertEquals("Consultant", consultant.node().label());
    assertEquals("display_name", consultant.property("name", String.class).property());
    assertThrows(MetadataException.class, () -> consultant.property("rating", String.class));
  }

  @Test
  void mapsNestedRecordsGenericCollectionsOptionalsAndCustomValues() {
    GraphValueConverters converters =
        GraphValueConverters.builder()
            .add(
                new GraphValueConverter() {
                  @Override
                  public boolean supports(Object source, Type targetType) {
                    return source instanceof String && targetType == URI.class;
                  }

                  @Override
                  public Object convert(Object source, Type targetType) {
                    return URI.create((String) source);
                  }
                })
            .build();
    RecordEntityMapper richMapper = new RecordEntityMapper(registry, converters);

    Profile profile =
        richMapper.map(
            Profile.class,
            Map.of(
                "nickname",
                "jules",
                "scores",
                List.of(1L, 2L),
                "address",
                Map.of("city", "Lisbon"),
                "website",
                "https://example.com"));

    assertEquals(
        new Profile(
            Optional.of("jules"),
            List.of(1, 2),
            new Address("Lisbon"),
            URI.create("https://example.com")),
        profile);
  }

  @GraphNode("Consultant")
  private record Consultant(
      @GraphId String id, @GraphProperty("display_name") String name, int rating, Status status) {}

  private record MultipleIds(@GraphId String first, @GraphId String second) {}

  private record Profile(
      Optional<String> nickname, List<Integer> scores, Address address, URI website) {}

  private record Address(String city) {}

  private static final class NonRecordType {}

  private enum Status {
    ACTIVE,
    INACTIVE
  }
}
