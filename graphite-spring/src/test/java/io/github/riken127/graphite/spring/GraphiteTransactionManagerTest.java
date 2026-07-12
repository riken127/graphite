package io.github.riken127.graphite.spring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.neo4j.GraphiteClient;
import io.github.riken127.graphite.neo4j.GraphiteExplicitTransaction;
import io.github.riken127.graphite.neo4j.QueryAccessMode;
import io.github.riken127.graphite.neo4j.QueryOptions;
import io.github.riken127.graphite.neo4j.QueryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

class GraphiteTransactionManagerTest {

  private final GraphiteClient client = mock(GraphiteClient.class);
  private final GraphiteExplicitTransaction transaction = mock(GraphiteExplicitTransaction.class);
  private final GraphiteTransactionManager transactionManager =
      new GraphiteTransactionManager(client);
  private final GraphiteSpringTemplate template = new GraphiteSpringTemplate(client);

  @AfterEach
  void clearSynchronization() {
    TransactionSynchronizationManager.clear();
  }

  @Test
  void templateUsesAndCommitsBoundTransaction() {
    Query query = mock(Query.class);
    QueryResult<?> result = mock(QueryResult.class);
    when(client.beginTransaction(eq(QueryAccessMode.WRITE), any(QueryOptions.class)))
        .thenReturn(transaction);
    when(transaction.isActive()).thenReturn(true);
    when(transaction.execute(query)).thenReturn(cast(result));

    new TransactionTemplate(transactionManager)
        .executeWithoutResult(ignored -> template.execute(query));

    verify(transaction).execute(query);
    verify(transaction).commit();
    assertFalse(TransactionSynchronizationManager.hasResource(client));
  }

  @Test
  void rollbackOnlyStatusRollsTransactionBack() {
    when(client.beginTransaction(eq(QueryAccessMode.WRITE), any(QueryOptions.class)))
        .thenReturn(transaction);
    when(transaction.isActive()).thenReturn(true);

    new TransactionTemplate(transactionManager)
        .executeWithoutResult(status -> status.setRollbackOnly());

    verify(transaction).rollback();
    assertFalse(TransactionSynchronizationManager.hasResource(client));
  }

  @Test
  void templateDelegatesToClientOutsideTransaction() {
    Query query = mock(Query.class);

    template.execute(query);

    verify(client).execute(query);
  }

  @SuppressWarnings("unchecked")
  private static <T> QueryResult<T> cast(QueryResult<?> result) {
    return (QueryResult<T>) result;
  }
}
