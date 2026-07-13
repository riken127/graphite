package example

import io.github.riken127.graphite.kotlin.*
import io.github.riken127.graphite.metadata.GraphNode

@GraphNode("Consultant")
data class KotlinConsultant(val name: String, val rating: Int)

fun queryFromKotlin() =
    query {
        val consultant = entity<KotlinConsultant>("c")
        val rating = consultant.property(KotlinConsultant::rating)
        match(path(consultant.node()))
        where(rating greaterThan 4)
        returning(rating aliasedAs "rating")
    }
