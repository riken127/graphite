package io.github.riken127.graphite.scala

import io.github.riken127.graphite.core.dsl.{ClauseQueryBuilder, Expressions, Graphite, PathBuilder, TypedPropertyRef}
import io.github.riken127.graphite.core.model.{ClauseQuery, NodePattern, PathPattern}
import io.github.riken127.graphite.core.model.clause.SetAssignment
import io.github.riken127.graphite.core.model.expression.{Expression, ExpressionSort, Projection}
import io.github.riken127.graphite.core.model.predicate.Predicate
import io.github.riken127.graphite.metadata.{GraphNode, GraphProperty}

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

/** Idiomatic Scala 3 entry points backed by Graphite's immutable Java AST. */
object GraphiteScala:

  /** Builds and validates a clause query. */
  def query(configure: ClauseQueryBuilder => Unit): ClauseQuery =
    val builder = Graphite.query()
    configure(builder)
    builder.build()

  /** Builds and validates a subquery with imported aliases. */
  def subquery(imports: String*)(configure: ClauseQueryBuilder => Unit): ClauseQuery =
    val builder = Graphite.subquery(imports*)
    configure(builder)
    builder.build()

  /** Builds an empty path for a single node. */
  def path(start: NodePattern): PathPattern = Graphite.path(start).build()

  /** Builds a path through a configured Java path builder. */
  def path(start: NodePattern)(configure: PathBuilder => Unit): PathPattern =
    val builder = Graphite.path(start)
    configure(builder)
    builder.build()

  /** Creates a typed property reference using a Scala ClassTag. */
  def property[T: ClassTag](alias: String, name: String): TypedPropertyRef[T] =
    Graphite.property(alias, name, runtimeClass[T])

  /** Creates a checked entity facade for a Scala type. */
  def entity[T: ClassTag](alias: String = "n"): ScalaEntity[T] = ScalaEntity(alias)

  extension [T](property: TypedPropertyRef[T])
    /** Typed equality predicate. */
    infix def ===(value: T): Predicate = property.eq(value)

    /** Typed non-equality predicate. */
    infix def =!=(value: T): Predicate = property.ne(value)

    /** Typed greater-than predicate. */
    infix def >(value: T): Predicate = property.gt(value)

    /** Typed membership predicate with Scala collection conversion. */
    infix def inside(values: Iterable[T]): Predicate = property.in(values.asJavaCollection)

    /** Parameterized typed assignment. */
    infix def setTo(value: T)(using ClassTag[T]): SetAssignment[T] =
      Expressions.set(property, Expressions.value(value, runtimeClass[T]))

  extension (expression: Expression[?])
    /** Unaliased projection. */
    def project: Projection = Projection.of(expression)

    /** Aliased projection. */
    infix def aliasedAs(alias: String): Projection = Projection.as(expression, alias)

    /** Ascending expression ordering. */
    def ascending: ExpressionSort = Expressions.asc(expression)

    /** Descending expression ordering. */
    def descending: ExpressionSort = Expressions.desc(expression)

  private def runtimeClass[T](using tag: ClassTag[T]): Class[T] =
    tag.runtimeClass.asInstanceOf[Class[T]]

/** Scala type and alias pair with runtime-checked, typed properties. */
final class ScalaEntity[T] private (val javaType: Class[T], val alias: String):
  private val label =
    Option(javaType.getAnnotation(classOf[GraphNode])).map(_.value()).getOrElse(javaType.getSimpleName)

  /** Returns this entity's node pattern. */
  def node: NodePattern = NodePattern(label, alias)

  /** Resolves and verifies a Scala field before creating a typed property. */
  def property[V: ClassTag](name: String): TypedPropertyRef[V] =
    val field = javaType.getDeclaredField(name)
    val expected = summon[ClassTag[V]].runtimeClass
    require(boxed(field.getType) == boxed(expected), s"property '$name' has type ${field.getType.getName}")
    val annotation = field.getAnnotation(classOf[GraphProperty])
    val graphName = if annotation == null then name else annotation.value()
    Graphite.property(alias, graphName, expected.asInstanceOf[Class[V]])

object ScalaEntity:
  /** Creates an entity facade using @GraphNode or the class simple name. */
  def apply[T: ClassTag](alias: String = "n"): ScalaEntity[T] =
    new ScalaEntity(summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]], alias)

private def boxed(value: Class[?]): Class[?] =
  if !value.isPrimitive then value
  else if value == java.lang.Integer.TYPE then classOf[java.lang.Integer]
  else if value == java.lang.Long.TYPE then classOf[java.lang.Long]
  else if value == java.lang.Boolean.TYPE then classOf[java.lang.Boolean]
  else if value == java.lang.Double.TYPE then classOf[java.lang.Double]
  else if value == java.lang.Float.TYPE then classOf[java.lang.Float]
  else if value == java.lang.Short.TYPE then classOf[java.lang.Short]
  else if value == java.lang.Byte.TYPE then classOf[java.lang.Byte]
  else if value == java.lang.Character.TYPE then classOf[java.lang.Character]
  else value
