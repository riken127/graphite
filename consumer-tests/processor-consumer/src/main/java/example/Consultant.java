package example;

import io.github.riken127.graphite.metadata.GraphId;
import io.github.riken127.graphite.metadata.GraphNode;
import io.github.riken127.graphite.metadata.GraphProperty;

/** Model used to prove metamodel generation from an external compiler invocation. */
@GraphNode("Consultant")
public record Consultant(@GraphId String id, @GraphProperty("display_name") String name) {}
