package io.github.riken127.graphite.neo4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Value returned from a managed transaction together with its causal bookmarks. */
public record TransactionResult<T>(T value, Set<String> bookmarks) {

  /** Creates an immutable managed-transaction result. */
  public TransactionResult {
    bookmarks =
        Collections.unmodifiableSet(
            new LinkedHashSet<>(Objects.requireNonNull(bookmarks, "bookmarks must not be null")));
  }
}
