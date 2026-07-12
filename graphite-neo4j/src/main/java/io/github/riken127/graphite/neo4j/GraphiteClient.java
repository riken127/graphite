package io.github.riken127.graphite.neo4j;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.MatchQuery;
import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.core.model.UnionQuery;
import io.github.riken127.graphite.core.model.clause.CallClause;
import io.github.riken127.graphite.core.model.clause.Clause;
import io.github.riken127.graphite.core.model.clause.CreateClause;
import io.github.riken127.graphite.core.model.clause.DeleteClause;
import io.github.riken127.graphite.core.model.clause.MergeClause;
import io.github.riken127.graphite.core.model.clause.RemoveClause;
import io.github.riken127.graphite.core.model.clause.SetClause;
import io.github.riken127.graphite.core.model.clause.SubqueryClause;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import io.github.riken127.graphite.cypher.renderer.CypherRenderer;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionConfig;

/**
 * Thread-safe facade for executing Graphite queries through the Neo4j Java Driver.
 *
 * <p>The caller owns the supplied {@link Driver} and remains responsible for closing it.
 */
public final class GraphiteClient implements GraphiteOperations {

  private final Driver driver;
  private final CypherRenderer renderer;
  private final QueryOptions defaultOptions;
  private final QueryObserver observer;

  public GraphiteClient(Driver driver) {
    this(driver, new CypherRenderer(), QueryOptions.defaults(), QueryObserver.noop());
  }

  /** Creates a client with explicit rendering and execution defaults. */
  public GraphiteClient(Driver driver, CypherRenderer renderer, QueryOptions defaultOptions) {
    this(driver, renderer, defaultOptions, QueryObserver.noop());
  }

  /** Creates a client with explicit defaults and a query observability hook. */
  public GraphiteClient(
      Driver driver, CypherRenderer renderer, QueryOptions defaultOptions, QueryObserver observer) {
    this.driver = Objects.requireNonNull(driver, "driver must not be null");
    this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
    this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions must not be null");
    this.observer = Objects.requireNonNull(observer, "observer must not be null");
  }

  /** Verifies that the configured driver can reach and authenticate with Neo4j. */
  public void verifyConnectivity() {
    try {
      driver.verifyConnectivity();
    } catch (RuntimeException failure) {
      throw DriverExceptionTranslator.translate(failure);
    }
  }

  @Override
  public QueryResult<Neo4jRecord> execute(Query query) {
    return execute(query, record -> record, defaultOptions);
  }

  @Override
  public <T> QueryResult<T> execute(Query query, Neo4jRecordMapper<T> mapper) {
    return execute(query, mapper, defaultOptions);
  }

  public QueryResult<Neo4jRecord> execute(Query query, QueryOptions options) {
    return execute(query, record -> record, options);
  }

  /** Executes a structured query with explicit mapping and execution options. */
  public <T> QueryResult<T> execute(
      Query query, Neo4jRecordMapper<T> mapper, QueryOptions options) {
    Objects.requireNonNull(query, "query must not be null");
    return executeRendered(
        renderer.render(query), mapper, options, resolveAccessMode(query, options));
  }

  @Override
  public QueryResult<Neo4jRecord> execute(RawCypherQuery query) {
    return execute(query, record -> record, defaultOptions);
  }

  @Override
  public <T> QueryResult<T> execute(RawCypherQuery query, Neo4jRecordMapper<T> mapper) {
    return execute(query, mapper, defaultOptions);
  }

  public QueryResult<Neo4jRecord> execute(RawCypherQuery query, QueryOptions options) {
    return execute(query, record -> record, options);
  }

  public <T> QueryResult<T> execute(
      RawCypherQuery query, Neo4jRecordMapper<T> mapper, QueryOptions options) {
    Objects.requireNonNull(query, "query must not be null");
    return executeRendered(renderer.render(query), mapper, options, resolveRawAccessMode(options));
  }

  /** Opens a closeable stream for a structured query using default options. */
  public StreamingQueryResult<Neo4jRecord> stream(Query query) {
    return stream(query, record -> record, defaultOptions);
  }

  /** Opens a mapped closeable stream for a structured query using default options. */
  public <T> StreamingQueryResult<T> stream(Query query, Neo4jRecordMapper<T> mapper) {
    return stream(query, mapper, defaultOptions);
  }

  /** Opens a mapped closeable stream for a structured query. */
  public <T> StreamingQueryResult<T> stream(
      Query query, Neo4jRecordMapper<T> mapper, QueryOptions options) {
    Objects.requireNonNull(query, "query must not be null");
    return streamRendered(
        renderer.render(query), mapper, options, resolveAccessMode(query, options));
  }

  /** Opens a closeable stream for raw Cypher using default write-safe routing. */
  public StreamingQueryResult<Neo4jRecord> stream(RawCypherQuery query) {
    return stream(query, record -> record, defaultOptions);
  }

  /** Opens a mapped closeable stream for raw Cypher using default write-safe routing. */
  public <T> StreamingQueryResult<T> stream(RawCypherQuery query, Neo4jRecordMapper<T> mapper) {
    return stream(query, mapper, defaultOptions);
  }

  /** Opens a closeable stream for raw Cypher using explicit routing options. */
  public StreamingQueryResult<Neo4jRecord> stream(RawCypherQuery query, QueryOptions options) {
    return stream(query, record -> record, options);
  }

  /** Opens a closeable stream for raw Cypher using explicit routing options. */
  public <T> StreamingQueryResult<T> stream(
      RawCypherQuery query, Neo4jRecordMapper<T> mapper, QueryOptions options) {
    Objects.requireNonNull(query, "query must not be null");
    return streamRendered(renderer.render(query), mapper, options, resolveRawAccessMode(options));
  }

  /** Executes retryable managed read work and returns its causal bookmarks. */
  public <T> TransactionResult<T> readTransaction(TransactionWork<T> work) {
    return readTransaction(work, defaultOptions);
  }

  /** Executes retryable managed read work with explicit session and transaction options. */
  public <T> TransactionResult<T> readTransaction(TransactionWork<T> work, QueryOptions options) {
    return managedTransaction(QueryAccessMode.READ, work, options);
  }

  /** Executes retryable managed write work and returns its causal bookmarks. */
  public <T> TransactionResult<T> writeTransaction(TransactionWork<T> work) {
    return writeTransaction(work, defaultOptions);
  }

  /** Executes retryable managed write work with explicit session and transaction options. */
  public <T> TransactionResult<T> writeTransaction(TransactionWork<T> work, QueryOptions options) {
    return managedTransaction(QueryAccessMode.WRITE, work, options);
  }

  /** Opens an explicit transaction. The caller must commit, roll back, or close it. */
  public GraphiteExplicitTransaction beginTransaction(
      QueryAccessMode accessMode, QueryOptions options) {
    if (accessMode == null || accessMode == QueryAccessMode.AUTO) {
      throw new IllegalArgumentException("explicit transaction accessMode must be READ or WRITE");
    }
    QueryOptions validatedOptions = Objects.requireNonNull(options, "options must not be null");
    Session session = driver.session(sessionConfig(validatedOptions, accessMode));
    try {
      org.neo4j.driver.Transaction transaction =
          session.beginTransaction(transactionConfig(validatedOptions));
      return new GraphiteExplicitTransaction(
          session, transaction, new DriverQueryExecutor(transaction, renderer, observer));
    } catch (RuntimeException failure) {
      session.close();
      throw DriverExceptionTranslator.translate(failure);
    }
  }

  private <T> QueryResult<T> executeRendered(
      RenderedQuery renderedQuery,
      Neo4jRecordMapper<T> mapper,
      QueryOptions options,
      QueryAccessMode accessMode) {
    Objects.requireNonNull(mapper, "mapper must not be null");
    QueryOptions validatedOptions = Objects.requireNonNull(options, "options must not be null");

    try (Session session = driver.session(sessionConfig(validatedOptions, accessMode))) {
      QueryResult<T> result =
          accessMode == QueryAccessMode.READ
              ? session.executeRead(
                  transaction -> executorFor(transaction).executeRendered(renderedQuery, mapper),
                  transactionConfig(validatedOptions))
              : session.executeWrite(
                  transaction -> executorFor(transaction).executeRendered(renderedQuery, mapper),
                  transactionConfig(validatedOptions));
      return result.withBookmarks(bookmarkValues(session.lastBookmarks()));
    } catch (RuntimeException failure) {
      throw DriverExceptionTranslator.translate(failure);
    }
  }

  private <T> StreamingQueryResult<T> streamRendered(
      RenderedQuery renderedQuery,
      Neo4jRecordMapper<T> mapper,
      QueryOptions options,
      QueryAccessMode accessMode) {
    Objects.requireNonNull(mapper, "mapper must not be null");
    QueryOptions validatedOptions = Objects.requireNonNull(options, "options must not be null");
    QueryObservation observation =
        QueryObservations.begin(observer, QueryObservations.descriptor(renderedQuery, true));
    Session session = null;
    org.neo4j.driver.Transaction transaction = null;
    try {
      session = driver.session(sessionConfig(validatedOptions, accessMode));
      transaction = session.beginTransaction(transactionConfig(validatedOptions));
      Result result = transaction.run(renderedQuery.cypher(), renderedQuery.parameters());
      return new StreamingQueryResult<>(session, transaction, result, mapper, observation);
    } catch (RuntimeException failure) {
      if (transaction != null) {
        try {
          transaction.close();
        } catch (RuntimeException closeFailure) {
          failure.addSuppressed(closeFailure);
        }
      }
      if (session != null) {
        try {
          session.close();
        } catch (RuntimeException closeFailure) {
          failure.addSuppressed(closeFailure);
        }
      }
      RuntimeException translated = DriverExceptionTranslator.translate(failure);
      QueryObservations.failed(observation, translated);
      throw translated;
    }
  }

  private <T> TransactionResult<T> managedTransaction(
      QueryAccessMode accessMode, TransactionWork<T> work, QueryOptions options) {
    Objects.requireNonNull(work, "work must not be null");
    QueryOptions validatedOptions = Objects.requireNonNull(options, "options must not be null");

    try (Session session = driver.session(sessionConfig(validatedOptions, accessMode))) {
      T value =
          accessMode == QueryAccessMode.READ
              ? session.executeRead(
                  transaction -> work.execute(executorFor(transaction)),
                  transactionConfig(validatedOptions))
              : session.executeWrite(
                  transaction -> work.execute(executorFor(transaction)),
                  transactionConfig(validatedOptions));
      return new TransactionResult<>(value, bookmarkValues(session.lastBookmarks()));
    } catch (RuntimeException failure) {
      throw DriverExceptionTranslator.translate(failure);
    }
  }

  private DriverQueryExecutor executorFor(org.neo4j.driver.TransactionContext transaction) {
    return new DriverQueryExecutor(transaction, renderer, observer);
  }

  static QueryAccessMode resolveAccessMode(Query query, QueryOptions options) {
    QueryAccessMode requested =
        Objects.requireNonNull(options, "options must not be null").accessMode();
    if (requested != QueryAccessMode.AUTO) {
      return requested;
    }
    return isReadOnly(query) ? QueryAccessMode.READ : QueryAccessMode.WRITE;
  }

  private static boolean isReadOnly(Query query) {
    if (query instanceof MatchQuery) {
      return true;
    }
    if (query instanceof ClauseQuery clauseQuery) {
      return clauseQuery.clauses().stream().allMatch(GraphiteClient::isReadOnly);
    }
    if (query instanceof UnionQuery unionQuery) {
      return unionQuery.branches().stream().allMatch(GraphiteClient::isReadOnly);
    }
    return false;
  }

  private static boolean isReadOnly(Clause clause) {
    if (clause instanceof CallClause callClause) {
      return callClause.readOnly();
    }
    if (clause instanceof SubqueryClause subqueryClause) {
      return isReadOnly(subqueryClause.query());
    }
    return !(clause instanceof CreateClause
        || clause instanceof MergeClause
        || clause instanceof SetClause
        || clause instanceof RemoveClause
        || clause instanceof DeleteClause);
  }

  private static QueryAccessMode resolveRawAccessMode(QueryOptions options) {
    QueryAccessMode requested =
        Objects.requireNonNull(options, "options must not be null").accessMode();
    return requested == QueryAccessMode.AUTO ? QueryAccessMode.WRITE : requested;
  }

  private static SessionConfig sessionConfig(QueryOptions options, QueryAccessMode accessMode) {
    SessionConfig.Builder builder =
        SessionConfig.builder()
            .withDefaultAccessMode(
                accessMode == QueryAccessMode.READ ? AccessMode.READ : AccessMode.WRITE);
    options.database().ifPresent(builder::withDatabase);
    options.fetchSize().ifPresent(builder::withFetchSize);
    options.impersonatedUser().ifPresent(builder::withImpersonatedUser);
    if (!options.bookmarks().isEmpty()) {
      builder.withBookmarks(options.bookmarks().stream().map(Bookmark::from).toList());
    }
    return builder.build();
  }

  private static TransactionConfig transactionConfig(QueryOptions options) {
    if (options.timeout().isEmpty() && options.metadata().isEmpty()) {
      return TransactionConfig.empty();
    }
    TransactionConfig.Builder builder = TransactionConfig.builder();
    options.timeout().ifPresent(builder::withTimeout);
    if (!options.metadata().isEmpty()) {
      builder.withMetadata(options.metadata());
    }
    return builder.build();
  }

  private static Set<String> bookmarkValues(Set<Bookmark> bookmarks) {
    Set<String> values = new LinkedHashSet<>();
    for (Bookmark bookmark : bookmarks) {
      values.add(bookmark.value());
    }
    return Set.copyOf(values);
  }
}
