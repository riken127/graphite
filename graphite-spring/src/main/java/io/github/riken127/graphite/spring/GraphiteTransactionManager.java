package io.github.riken127.graphite.spring;

import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.GraphiteExplicitTransaction;
import io.github.riken127.graphite.neo4j.QueryAccessMode;
import io.github.riken127.graphite.neo4j.QueryOptions;
import java.io.Serial;
import java.time.Duration;
import java.util.Objects;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Spring transaction manager backed by explicit Neo4j driver transactions. */
public final class GraphiteTransactionManager extends AbstractPlatformTransactionManager {

  @Serial private static final long serialVersionUID = 1L;

  private final GraphiteClient client;
  private final QueryOptions defaultOptions;

  public GraphiteTransactionManager(GraphiteClient client) {
    this(client, QueryOptions.defaults());
  }

  public GraphiteTransactionManager(GraphiteClient client, QueryOptions defaultOptions) {
    this.client = Objects.requireNonNull(client, "client must not be null");
    this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions must not be null");
  }

  @Override
  protected Object doGetTransaction() {
    return new TransactionObject(currentHolder());
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) {
    TransactionObject transactionObject = (TransactionObject) transaction;
    return transactionObject.holder != null && transactionObject.holder.transaction().isActive();
  }

  @Override
  protected void doBegin(Object transaction, TransactionDefinition definition) {
    TransactionObject transactionObject = (TransactionObject) transaction;
    QueryAccessMode accessMode =
        definition.isReadOnly() ? QueryAccessMode.READ : QueryAccessMode.WRITE;
    QueryOptions.Builder options = QueryOptions.builder(defaultOptions).accessMode(accessMode);
    if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
      options.timeout(Duration.ofSeconds(definition.getTimeout()));
    }

    GraphiteExplicitTransaction explicitTransaction =
        client.beginTransaction(accessMode, options.build());
    GraphiteTransactionHolder holder = new GraphiteTransactionHolder(explicitTransaction);
    holder.setSynchronizedWithTransaction(true);
    transactionObject.holder = holder;
    TransactionSynchronizationManager.bindResource(client, holder);
  }

  @Override
  protected void doCommit(DefaultTransactionStatus status) {
    transaction(status).commit();
  }

  @Override
  protected void doRollback(DefaultTransactionStatus status) {
    transaction(status).rollback();
  }

  @Override
  protected void doSetRollbackOnly(DefaultTransactionStatus status) {
    transactionObject(status).holder.setRollbackOnly();
  }

  @Override
  protected Object doSuspend(Object transaction) {
    TransactionObject transactionObject = (TransactionObject) transaction;
    transactionObject.holder = null;
    return TransactionSynchronizationManager.unbindResource(client);
  }

  @Override
  protected void doResume(Object transaction, Object suspendedResources) {
    TransactionObject transactionObject = (TransactionObject) transaction;
    GraphiteTransactionHolder holder = (GraphiteTransactionHolder) suspendedResources;
    transactionObject.holder = holder;
    TransactionSynchronizationManager.bindResource(client, holder);
  }

  @Override
  protected void doCleanupAfterCompletion(Object transaction) {
    TransactionObject transactionObject = (TransactionObject) transaction;
    if (TransactionSynchronizationManager.hasResource(client)) {
      TransactionSynchronizationManager.unbindResource(client);
    }
    if (transactionObject.holder != null) {
      transactionObject.holder.transaction().close();
      transactionObject.holder.clear();
      transactionObject.holder = null;
    }
  }

  private GraphiteTransactionHolder currentHolder() {
    Object resource = TransactionSynchronizationManager.getResource(client);
    return resource instanceof GraphiteTransactionHolder holder ? holder : null;
  }

  private static TransactionObject transactionObject(DefaultTransactionStatus status) {
    return (TransactionObject) status.getTransaction();
  }

  private static GraphiteExplicitTransaction transaction(DefaultTransactionStatus status) {
    return transactionObject(status).holder.transaction();
  }

  private static final class TransactionObject implements SmartTransactionObject {

    private GraphiteTransactionHolder holder;

    private TransactionObject(GraphiteTransactionHolder holder) {
      this.holder = holder;
    }

    @Override
    public boolean isRollbackOnly() {
      return holder != null && holder.isRollbackOnly();
    }

    @Override
    public void flush() {}
  }
}
