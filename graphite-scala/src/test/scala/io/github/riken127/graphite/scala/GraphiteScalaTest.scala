package io.github.riken127.graphite.scala

import io.github.riken127.graphite.metadata.GraphNode
import io.github.riken127.graphite.scala.GraphiteScala.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

final class GraphiteScalaTest:

  @Test
  def buildsTypedQueriesFromScalaCaseClasses(): Unit =
    val consultant = entity[Consultant]("c")
    val rating = consultant.property[Int]("rating")
    val name = consultant.property[String]("name")

    val built = query { builder =>
      builder.`match`(path(consultant.node))
      builder.where(rating > 4)
      builder.set(name setTo "Ada")
      builder.returning(name aliasedAs "name")
      builder.orderBy(name.ascending)
    }

    assertEquals("ConsultantNode", consultant.node.label())
    assertEquals(classOf[Int], rating.valueType())
    assertEquals(5, built.clauses().size())

  @GraphNode("ConsultantNode")
  private final case class Consultant(name: String, rating: Int)
