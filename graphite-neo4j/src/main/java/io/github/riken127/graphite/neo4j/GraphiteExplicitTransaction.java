package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

/**
 * Explicit transaction for framework integrations and manual commit/rollback workflows.
 *
 * <p>Instances are not thread-safe. Closing an active transaction rolls it back.
 */
public final class GraphiteExplicitTransaction implements GraphiteOperations, AutoCloseable {

  private final Session session;
  private final Transaction transaction;
  private final DriverQueryExecutor executor;
  private State state = State.ACTIVE;
  private Set<String> bookmarks = Set.of();

  GraphiteExplicitTransaction(
      Session session, Transaction transaction, DriverQueryExecutor executor) {
    this.session = Objects.requireNonNull(session, "session must not be null");
    this.transaction = Objects.requireNonNull(transaction, "transaction must not be null");
    this.executor = Objects.requireNonNull(executor, "executor must not be null");
  }

  @Override
  public QueryResult<Neo4jRecord> execute(Query query) {
    requireActive();
    return executor.execute(query);
  }

  @Override
  public <T> QueryResult<T> execute(Query query, Neo4jRecordMapper<T> mapper) {
    requireActive();
    return executor.execute(query, mapper);
  }

  @Override
  public QueryResult<Neo4jRecord> execute(RawCypherQuery query) {
    requireActive();
    return executor.execute(query);
  }

  @Override
  public <T> QueryResult<T> execute(RawCypherQuery query, Neo4jRecordMapper<T> mapper) {
    requireActive();
    return executor.execute(query, mapper);
  }

  /** Commits the transaction and releases its session. */
  public void commit() {
    requireActive();
    try {
      transaction.commit();
      state = State.COMMITTED;
      captureBookmarks();
    } catch (RuntimeException failure) {
      state = State.FAILED;
      closeResourcesSuppressing(failure);
      throw DriverExceptionTranslator.translate(failure);
    }
    closeResourcesTranslated();
  }

  /** Rolls the transaction back and releases its session. */
  public void rollback() {
    requireActive();
    try {
      transaction.rollback();
      state = State.ROLLED_BACK;
    } catch (RuntimeException failure) {
      state = State.FAILED;
      closeResourcesSuppressing(failure);
      throw DriverExceptionTranslator.translate(failure);
    }
    closeResourcesTranslated();
  }

  public boolean isActive() {
    return state == State.ACTIVE;
  }

  public Set<String> bookmarks() {
    return bookmarks;
  }

  @Override
  public void close() {
    if (isActive()) {
      try {
        transaction.rollback();
        state = State.ROLLED_BACK;
      } catch (RuntimeException failure) {
        state = State.FAILED;
        closeResourcesSuppressing(failure);
        throw DriverExceptionTranslator.translate(failure);
      }
      closeResourcesTranslated();
    }
  }

  private void captureBookmarks() {
    Set<String> values = new LinkedHashSet<>();
    for (Bookmark bookmark : session.lastBookmarks()) {
      values.add(bookmark.value());
    }
    bookmarks = Collections.unmodifiableSet(values);
  }

  private void closeResources() {
    try {
      transaction.close();
    } finally {
      session.close();
    }
  }

  private void closeResourcesTranslated() {
    try {
      closeResources();
    } catch (RuntimeException failure) {
      throw DriverExceptionTranslator.translate(failure);
    }
  }

  private void closeResourcesSuppressing(RuntimeException primaryFailure) {
    try {
      closeResources();
    } catch (RuntimeException closeFailure) {
      primaryFailure.addSuppressed(closeFailure);
    }
  }

  private void requireActive() {
    if (!isActive()) {
      throw new IllegalStateException("transaction is no longer active: " + state);
    }
  }

  private enum State {
    ACTIVE,
    COMMITTED,
    ROLLED_BACK,
    FAILED
  }
}
