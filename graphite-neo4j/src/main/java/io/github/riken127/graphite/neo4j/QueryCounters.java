package io.github.riken127.graphite.neo4j;

/** Database update counters reported by Neo4j. */
public record QueryCounters(
    boolean containsUpdates,
    int nodesCreated,
    int nodesDeleted,
    int relationshipsCreated,
    int relationshipsDeleted,
    int propertiesSet,
    int labelsAdded,
    int labelsRemoved,
    int indexesAdded,
    int indexesRemoved,
    int constraintsAdded,
    int constraintsRemoved,
    boolean containsSystemUpdates,
    int systemUpdates) {}
