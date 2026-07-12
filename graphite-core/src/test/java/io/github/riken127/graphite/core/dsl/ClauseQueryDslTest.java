package io.github.riken127.graphite.core.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.UnionQuery;
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

  @Test
  void buildsWritePipelinesWithoutRequiringReturn() {
    PathPattern person = Graphite.path(Graphite.node("Person").as("p")).build();
    TypedPropertyRef<String> name = Graphite.property("p", "name", String.class);

    ClauseQuery query =
        Graphite.query()
            .match(person)
            .set(Expressions.set(name, Expressions.value("Ada", String.class)))
            .remove(Graphite.property("p", "legacyName", String.class))
            .build();

    assertEquals(3, query.clauses().size());
  }

  @Test
  void validatesImportedSubqueryScopeAndUnionOutputs() {
    PathPattern person = Graphite.path(Graphite.node("Person").as("p")).build();
    ClauseQuery nested =
        Graphite.subquery("p")
            .returning(Projection.as(Graphite.property("p", "name", String.class), "personName"))
            .build();
    ClauseQuery outer =
        Graphite.query()
            .match(person)
            .subquery(nested, List.of("p"), "personName")
            .returning(Projection.as(Expressions.variable("personName", String.class), "result"))
            .build();
    ClauseQuery second =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Company").as("c")).build())
            .returning(Projection.as(Graphite.property("c", "name", String.class), "result"))
            .build();

    UnionQuery union = Graphite.unionAll(outer, second);

    assertEquals(2, union.branches().size());
  }

  @Test
  void rejectsMismatchedUnionColumns() {
    ClauseQuery first =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Person").as("p")).build())
            .returning(Projection.as(Expressions.variable("p", Object.class), "person"))
            .build();
    ClauseQuery second =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Company").as("c")).build())
            .returning(Projection.as(Expressions.variable("c", Object.class), "company"))
            .build();

    assertThrows(IllegalArgumentException.class, () -> Graphite.union(first, second));
  }
}
