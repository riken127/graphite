package io.github.riken127.graphite.core.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.expression.Projection;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClauseQueryDslTest {

  @Test
  void composesMultipleOptionalAndScopedClauses() {
    PathPattern consultants = Graphite.path(Graphite.node("Consultant").as("c")).build();
    PathPattern managers = Graphite.path(Graphite.node("Manager").as("m")).build();
    PathPattern referrals =
        Graphite.path(Graphite.node("Consultant").as("c"))
            .out("REFERRED_BY", Graphite.node("Manager").as("manager"))
            .build();
    TypedPropertyRef<String> consultantId = Graphite.property("c", "id", String.class);

    ClauseQuery query =
        Graphite.query()
            .match(consultants, managers)
            .where(consultantId.eq("42"))
            .optionalMatch(referrals)
            .with(
                Projection.of(Expressions.variable("c", Object.class)),
                Projection.as(Graphite.property("manager", "name", String.class), "managerName"))
            .unwind(Expressions.value(List.of("java", "neo4j"), List.class), "skill")
            .returning(
                true,
                Projection.of(Graphite.property("c", "name", String.class)),
                Projection.of(Expressions.variable("skill", String.class)))
            .orderBy(Expressions.asc(Graphite.property("c", "name", String.class)))
            .limit(25)
            .build();

    assertEquals(8, query.clauses().size());
  }

  @Test
  void rejectsReferencesRemovedByWithScope() {
    PathPattern consultants = Graphite.path(Graphite.node("Consultant").as("c")).build();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            Graphite.query()
                .match(consultants)
                .with(Projection.as(Graphite.property("c", "name", String.class), "name"))
                .returning(Projection.of(Graphite.property("c", "id", String.class)))
                .build());
  }
}
