package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Expressions;
import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClauseQueryRendererTest {

  @Test
  void rendersTypedGeneralClausePipeline() {
    PathPattern referrals =
        Graphite.path(Graphite.node("Consultant").as("c"))
            .out("REFERRED_BY", Graphite.node("Consultant").as("m"))
            .build();
    TypedPropertyRef<String> id = Graphite.property("c", "id", String.class);
    TypedPropertyRef<String> managerName = Graphite.property("m", "name", String.class);
    ClauseQuery query =
        Graphite.query()
            .match(referrals)
            .where(id.eq("42"))
            .with(
                Projection.of(Expressions.variable("m", Object.class)),
                Projection.as(managerName, "name"))
            .unwind(Expressions.value(List.of("java", "neo4j"), List.class), "skill")
            .returning(
                true,
                Projection.of(Expressions.variable("name", String.class)),
                Projection.of(Expressions.variable("skill", String.class)))
            .orderBy(Expressions.desc(Expressions.variable("name", String.class)))
            .skip(5)
            .limit(10)
            .build();

    RenderedQuery rendered = new CypherRenderer().render(query);

    assertEquals(
        "MATCH (c:Consultant)-[:REFERRED_BY]->(m:Consultant) WHERE c.id = $p0 "
            + "WITH m, m.name AS name UNWIND $p1 AS skill RETURN DISTINCT name, skill "
            + "ORDER BY name DESC SKIP 5 LIMIT 10",
        rendered.cypher());
    assertEquals(Map.of("p0", "42", "p1", List.of("java", "neo4j")), rendered.parameters());
  }

  @Test
  void rendersPropertyToPropertyComparison() {
    PathPattern people = Graphite.path(Graphite.node("Person").as("p")).build();
    ClauseQuery query =
        Graphite.query()
            .match(people)
            .where(
                Graphite.property("p", "firstName", String.class)
                    .eq(Graphite.property("p", "displayName", String.class)))
            .returning(Projection.of(Expressions.variable("p", Object.class)))
            .build();

    assertEquals(
        "MATCH (p:Person) WHERE p.firstName = p.displayName RETURN p",
        new CypherRenderer().render(query).cypher());
  }

  @Test
  void rendersWritesAndRichExpressions() {
    PathPattern person = Graphite.path(Graphite.node("Person").as("p")).build();
    TypedPropertyRef<String> status = Graphite.property("p", "status", String.class);
    LinkedHashMap<String, io.github.riken127.graphite.core.model.expression.Expression<?>> values =
        new LinkedHashMap<>();
    values.put("status", status);
    values.put(
        "labels",
        Expressions.list(
            String.class,
            Expressions.value("active", String.class),
            Expressions.value("verified", String.class)));

    ClauseQuery query =
        Graphite.query()
            .merge(
                person,
                List.of(Expressions.set(status, Expressions.value("new", String.class))),
                List.of(Expressions.set(status, Expressions.value("existing", String.class))))
            .set(
                Expressions.set(
                    Graphite.property("p", "displayStatus", String.class),
                    Expressions.caseWhen(
                        String.class,
                        Expressions.value("other", String.class),
                        Expressions.when(
                            status.eq("new"), Expressions.value("created", String.class)))))
            .returning(Projection.as(Expressions.map(values), "details"))
            .build();

    RenderedQuery rendered = new CypherRenderer().render(query);

    assertEquals(
        "MERGE (p:Person) ON CREATE SET p.status = $p0 ON MATCH SET p.status = $p1 "
            + "SET p.displayStatus = CASE WHEN p.status = $p2 THEN $p3 ELSE $p4 END "
            + "RETURN {status: p.status, labels: [$p5, $p6]} AS details",
        rendered.cypher());
    assertEquals(
        Map.of(
            "p0", "new",
            "p1", "existing",
            "p2", "new",
            "p3", "created",
            "p4", "other",
            "p5", "active",
            "p6", "verified"),
        rendered.parameters());
  }

  @Test
  void rendersProceduresSubqueriesAndUnion() {
    PathPattern person = Graphite.path(Graphite.node("Person").as("p")).build();
    ClauseQuery nested =
        Graphite.subquery("p")
            .returning(Projection.as(Graphite.property("p", "name", String.class), "entityName"))
            .build();
    ClauseQuery people =
        Graphite.query()
            .match(person)
            .callReadOnly("db.labels", List.of(), "label")
            .subquery(nested, List.of("p"), "entityName")
            .returning(Projection.as(Expressions.variable("entityName", String.class), "name"))
            .build();
    ClauseQuery companies =
        Graphite.query()
            .match(Graphite.path(Graphite.node("Company").as("c")).build())
            .returning(Projection.as(Graphite.property("c", "name", String.class), "name"))
            .build();

    RenderedQuery rendered = new CypherRenderer().render(Graphite.union(people, companies));

    assertEquals(
        "MATCH (p:Person) CALL db.labels() YIELD label CALL { WITH p RETURN p.name AS entityName } "
            + "RETURN entityName AS name UNION MATCH (c:Company) RETURN c.name AS name",
        rendered.cypher());
  }
}
