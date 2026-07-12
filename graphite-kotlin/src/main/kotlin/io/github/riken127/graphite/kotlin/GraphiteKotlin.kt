@file:JvmName("GraphiteKotlin")

package io.github.riken127.graphite.kotlin

import io.github.riken127.graphite.core.dsl.ClauseQueryBuilder
import io.github.riken127.graphite.core.dsl.Expressions
import io.github.riken127.graphite.core.dsl.Graphite
import io.github.riken127.graphite.core.dsl.PathBuilder
import io.github.riken127.graphite.core.dsl.TypedPropertyRef
import io.github.riken127.graphite.core.model.ClauseQuery
import io.github.riken127.graphite.core.model.NodePattern
import io.github.riken127.graphite.core.model.PathPattern
import io.github.riken127.graphite.core.model.clause.SetAssignment
import io.github.riken127.graphite.core.model.expression.Expression
import io.github.riken127.graphite.core.model.expression.ExpressionSort
import io.github.riken127.graphite.core.model.expression.Projection
import io.github.riken127.graphite.core.model.predicate.Predicate
import io.github.riken127.graphite.metadata.GraphNode
import io.github.riken127.graphite.metadata.GraphProperty
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.jvmErasure

/** Builds and validates a clause query with a Kotlin receiver lambda. */
public fun query(block: ClauseQueryBuilder.() -> Unit): ClauseQuery =
    Graphite.query().apply(block).build()

/** Builds and validates a scoped subquery with imported aliases. */
public fun subquery(vararg imports: String, block: ClauseQueryBuilder.() -> Unit): ClauseQuery =
    Graphite.subquery(*imports).apply(block).build()

/** Builds a path with a Kotlin receiver lambda. */
public fun path(start: NodePattern, block: PathBuilder.() -> Unit = {}): PathPattern =
    Graphite.path(start).apply(block).build()

/** Creates a reified typed property reference. */
public inline fun <reified T : Any> property(alias: String, name: String): TypedPropertyRef<T> =
    Graphite.property(alias, name, T::class.java)

/** Creates a metadata-aware entity facade for a Kotlin class. */
public inline fun <reified T : Any> entity(alias: String = "n"): KotlinEntity<T> =
    KotlinEntity(T::class, alias)

/** Kotlin class and alias pair that accepts refactor-safe property references. */
public class KotlinEntity<T : Any>(
    public val type: KClass<T>,
    public val alias: String = "n",
) {
    private val label: String = type.findAnnotation<GraphNode>()?.value ?: requireSimpleName(type)

    /** Returns this entity's node pattern. */
    public fun node(): NodePattern = NodePattern(label, alias)

    /** Resolves a Kotlin property reference into a typed graph property expression. */
    @Suppress("UNCHECKED_CAST")
    public fun <V> property(property: KProperty1<T, V>): TypedPropertyRef<V> {
        val field = property.javaField
        val graphName =
            field?.getAnnotation(GraphProperty::class.java)?.value
                ?: property.javaGetter?.getAnnotation(GraphProperty::class.java)?.value
                ?: property.name
        val valueType = property.returnType.jvmErasure.java as Class<V>
        validateDeclaredType(field, valueType, property.name)
        return Graphite.property(alias, graphName, valueType)
    }

    private fun <V> validateDeclaredType(field: Field?, valueType: Class<V>, propertyName: String) {
        if (field != null && boxed(field.type) != boxed(valueType)) {
            throw IllegalArgumentException("property '$propertyName' has inconsistent runtime type")
        }
    }
}

/** Builds a typed equality predicate. */
public infix fun <T> TypedPropertyRef<T>.eqTo(value: T): Predicate = eq(value)

/** Builds a typed non-equality predicate. */
public infix fun <T> TypedPropertyRef<T>.notEqTo(value: T): Predicate = ne(value)

/** Builds a typed greater-than predicate. */
public infix fun <T> TypedPropertyRef<T>.greaterThan(value: T): Predicate = gt(value)

/** Builds a typed collection-membership predicate. */
public infix fun <T> TypedPropertyRef<T>.inside(values: Iterable<T>): Predicate = `in`(values.toList())

/** Creates an unaliased projection. */
public fun Expression<*>.project(): Projection = Projection.of(this)

/** Creates an aliased projection. */
public infix fun Expression<*>.aliasedAs(alias: String): Projection = Projection.`as`(this, alias)

/** Creates ascending sort criteria. */
public fun Expression<*>.ascending(): ExpressionSort = Expressions.asc(this)

/** Creates descending sort criteria. */
public fun Expression<*>.descending(): ExpressionSort = Expressions.desc(this)

/** Creates an assignment from two compatible typed expressions. */
public infix fun <T> Expression<T>.setTo(value: Expression<out T>): SetAssignment<T> =
    Expressions.set(this, value)

/** Creates a parameterized typed assignment. */
public inline infix fun <reified T : Any> TypedPropertyRef<T>.setTo(value: T): SetAssignment<T> =
    Expressions.set(this, Expressions.value(value, T::class.java))

private fun requireSimpleName(type: KClass<*>): String =
    requireNotNull(type.simpleName) { "anonymous and local classes require @GraphNode" }

private fun boxed(type: Class<*>): Class<*> =
    when (type) {
        java.lang.Integer.TYPE -> java.lang.Integer::class.java
        java.lang.Long.TYPE -> java.lang.Long::class.java
        java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        java.lang.Double.TYPE -> java.lang.Double::class.java
        java.lang.Float.TYPE -> java.lang.Float::class.java
        java.lang.Short.TYPE -> java.lang.Short::class.java
        java.lang.Byte.TYPE -> java.lang.Byte::class.java
        java.lang.Character.TYPE -> java.lang.Character::class.java
        else -> type
    }
