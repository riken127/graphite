package example

import io.github.riken127.graphite.metadata.GraphNode
import io.github.riken127.graphite.scala.GraphiteScala.*

@GraphNode("Consultant")
final case class ScalaConsultant(name: String, rating: Int)

object ScalaConsumer:
  def queryFromScala =
    val consultant = entity[ScalaConsultant]("c")
    val rating = consultant.property[Int]("rating")
    query { builder =>
      builder.`match`(path(consultant.node))
      builder.where(rating > 4)
      builder.returning(rating aliasedAs "rating")
    }
