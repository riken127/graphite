package io.github.riken127.graphite.spring;

import io.github.riken127.graphite.neo4j.GraphiteExplicitTransaction;
import java.util.Objects;
import org.springframework.transaction.support.ResourceHolderSupport;

final class GraphiteTransactionHolder extends ResourceHolderSupport {

  private final GraphiteExplicitTransaction transaction;

  GraphiteTransactionHolder(GraphiteExplicitTransaction transaction) {
    this.transaction = Objects.requireNonNull(transaction, "transaction must not be null");
  }

  GraphiteExplicitTransaction transaction() {
    return transaction;
  }
}
