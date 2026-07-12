package io.github.riken127.graphite.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QueryOptionsTest {

  @Test
  void builderCopiesMutableInputs() {
    Map<String, Object> metadata = new LinkedHashMap<>(Map.of("trace", "abc"));
    Set<String> bookmarks = new LinkedHashSet<>(Set.of("bookmark-1"));

    QueryOptions options =
        QueryOptions.builder()
            .database("neo4j")
            .accessMode(QueryAccessMode.READ)
            .timeout(Duration.ofSeconds(2))
            .metadata(metadata)
            .fetchSize(100)
            .bookmarks(bookmarks)
            .impersonatedUser("reader")
            .build();
    metadata.put("later", true);
    bookmarks.add("bookmark-2");

    assertEquals(Map.of("trace", "abc"), options.metadata());
    assertEquals(Set.of("bookmark-1"), options.bookmarks());
    assertEquals(100, options.fetchSize().orElseThrow());
  }

  @Test
  void builderRejectsUnsafeOperationalValues() {
    assertThrows(IllegalArgumentException.class, () -> QueryOptions.builder().database(" "));
    assertThrows(
        IllegalArgumentException.class, () -> QueryOptions.builder().timeout(Duration.ZERO));
    assertThrows(IllegalArgumentException.class, () -> QueryOptions.builder().fetchSize(0));
  }
}
