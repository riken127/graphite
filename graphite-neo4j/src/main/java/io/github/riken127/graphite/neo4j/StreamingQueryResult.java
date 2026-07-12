package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.neo4j.exception.GraphiteMappingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;

/**
 * Closeable, pull-based query result that keeps its Neo4j transaction open while records are read.
 *
 * <p>Instances are single-use and not thread-safe. Exhaustion commits the read transaction; closing
 * before exhaustion rolls it back and releases all resources.
 */
public final class StreamingQueryResult<T> implements Iterable<T>, Iterator<T>, AutoCloseable {

  private final Session session;
  private final Transaction transaction;
  private final Result result;
  private final Neo4jRecordMapper<T> mapper;
  private final QueryObservation observation;

  private State state = State.ACTIVE;
  private QuerySummary summary;
  private Set<String> bookmarks = Set.of();
  private long recordCount;

  StreamingQueryResult(
      Session session,
      Transaction transaction,
      Result result,
      Neo4jRecordMapper<T> mapper,
      QueryObservation observation) {
    this.session = session;
    this.transaction = transaction;
    this.result = result;
    this.mapper = mapper;
    this.observation = observation;
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    if (state != State.ACTIVE) {
      return false;
    }
    try {
      boolean available = result.hasNext();
      if (!available) {
        finish();
      }
      return available;
    } catch (RuntimeException failure) {
      throw handleFailure(failure);
    }
  }

  @Override
  public T next() {
    if (!hasNext()) {
      throw new NoSuchElementException("streaming query result is exhausted");
    }
    try {
      Record record = result.next();
      T mapped = map(record);
      recordCount++;
      if (!result.hasNext()) {
        finish();
      }
      return mapped;
    } catch (RuntimeException failure) {
      throw handleFailure(failure);
    }
  }

  /** Returns a Java stream that closes this result when explicitly closed. */
  public Stream<T> stream() {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED | Spliterator.NONNULL),
            false)
        .onClose(this::close);
  }

  /** Returns the execution summary after successful exhaustion. */
  public Optional<QuerySummary> summary() {
    return Optional.ofNullable(summary);
  }

  /** Returns causal bookmarks after successful exhaustion. */
  public Set<String> bookmarks() {
    return bookmarks;
  }

  /** Returns whether every record was consumed and the transaction committed. */
  public boolean completed() {
    return state == State.COMPLETED;
  }

  /** Returns the number of records mapped so far. */
  public long recordCount() {
    return recordCount;
  }

  @Override
  public void close() {
    if (state != State.ACTIVE) {
      return;
    }
    try {
      transaction.rollback();
      state = State.CANCELLED;
      closeResources();
      QueryObservations.cancelled(observation, recordCount);
    } catch (RuntimeException failure) {
      throw handleFailure(failure);
    }
  }

  private T map(Record record) {
    try {
      return mapper.map(new Neo4jRecord(record));
    } catch (Neo4jException failure) {
      throw failure;
    } catch (RuntimeException failure) {
      throw new GraphiteMappingException("failed to map Neo4j streaming result record", failure);
    }
  }

  private void finish() {
    if (state != State.ACTIVE) {
      return;
    }
    summary = DriverSummaryMapper.map(result.consume());
    transaction.commit();
    captureBookmarks();
    state = State.COMPLETED;
    closeResources();
    QueryObservations.succeeded(observation, summary, recordCount);
  }

  private RuntimeException handleFailure(RuntimeException failure) {
    RuntimeException translated = DriverExceptionTranslator.translate(failure);
    if (state != State.ACTIVE) {
      QueryObservations.failed(observation, translated);
      return translated;
    }
    state = State.FAILED;
    try {
      transaction.rollback();
    } catch (RuntimeException rollbackFailure) {
      translated.addSuppressed(rollbackFailure);
    }
    try {
      closeResources();
    } catch (RuntimeException closeFailure) {
      translated.addSuppressed(closeFailure);
    }
    QueryObservations.failed(observation, translated);
    return translated;
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

  private enum State {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    FAILED
  }
}
