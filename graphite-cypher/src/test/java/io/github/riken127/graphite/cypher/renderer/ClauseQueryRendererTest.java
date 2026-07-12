package io.github.riken127.graphite.cypher.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.riken127.graphite.core.dsl.Expressions;
import io.github.riken127.graphite.core.dsl.Graphite;
import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.core.model.ClauseQuery;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
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
}
