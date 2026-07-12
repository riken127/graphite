package io.github.riken127.graphite.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Expressions;
import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.expression.Projection;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphiteClientRoutingTest {

  @Test
  void routesStructuredWritesAndUnionsSafely() {
    PathPattern person = Graphite.path(Graphite.node("Person").as("p")).build();
    ClauseQuery read =
        Graphite.query()
            .match(person)
            .returning(Projection.as(Expressions.variable("p", Object.class), "value"))
            .build();
    ClauseQuery write =
        Graphite.query()
            .match(person)
            .set(
                Expressions.set(
                    Graphite.property("p", "name", String.class),
                    Expressions.value("Ada", String.class)))
            .returning(Projection.as(Expressions.variable("p", Object.class), "value"))
            .build();

    assertEquals(
        QueryAccessMode.READ, GraphiteClient.resolveAccessMode(read, QueryOptions.defaults()));
    assertEquals(
        QueryAccessMode.WRITE, GraphiteClient.resolveAccessMode(write, QueryOptions.defaults()));
    assertEquals(
        QueryAccessMode.WRITE,
        GraphiteClient.resolveAccessMode(Graphite.union(read, write), QueryOptions.defaults()));
  }

  @Test
  void treatsUnclassifiedProceduresAsWrites() {
    ClauseQuery safeCall =
        Graphite.query()
            .callReadOnly("db.labels", List.of(), "label")
            .returning(Projection.as(Expressions.variable("label", String.class), "label"))
            .build();
    ClauseQuery conservativeCall = Graphite.query().call("custom.refresh", List.of()).build();

    assertEquals(
        QueryAccessMode.READ, GraphiteClient.resolveAccessMode(safeCall, QueryOptions.defaults()));
    assertEquals(
        QueryAccessMode.WRITE,
        GraphiteClient.resolveAccessMode(conservativeCall, QueryOptions.defaults()));
  }
}
