package io.github.riken127.graphite.kotlin

import io.github.riken127.graphite.metadata.GraphNode
import io.github.riken127.graphite.metadata.GraphProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GraphiteKotlinTest {
    @Test
    fun `builds typed queries from Kotlin property references`() {
        val consultant = entity<Consultant>("c")
        val rating = consultant.property(Consultant::rating)
        val displayName = consultant.property(Consultant::name)

        val built =
            query {
                match(path(consultant.node()))
                where(rating greaterThan 4)
                set(displayName setTo "Ada")
                returning(displayName aliasedAs "name")
                orderBy(displayName.ascending())
            }

        assertEquals("ConsultantNode", consultant.node().label())
        assertEquals("display_name", displayName.property())
        assertEquals(5, built.clauses().size)
    }

    @GraphNode("ConsultantNode")
    private data class Consultant(
        @field:GraphProperty("display_name") val name: String,
        val rating: Int,
    )
}
