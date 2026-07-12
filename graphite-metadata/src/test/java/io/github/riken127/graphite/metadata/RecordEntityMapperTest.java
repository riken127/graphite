package io.github.riken127.graphite.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
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

  @GraphNode("Consultant")
  private record Consultant(
      @GraphId String id, @GraphProperty("display_name") String name, int rating, Status status) {}

  private record MultipleIds(@GraphId String first, @GraphId String second) {}

  private static final class NonRecordType {}

  private enum Status {
    ACTIVE,
    INACTIVE
  }
}
