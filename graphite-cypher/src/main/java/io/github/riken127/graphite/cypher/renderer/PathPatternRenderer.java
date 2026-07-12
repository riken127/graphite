package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.NodePattern;
import io.github.riken127.graphite.core.model.PathPattern;
import io.github.riken127.graphite.core.model.RelationshipPattern;
import io.github.riken127.graphite.core.model.Traversal;

final class PathPatternRenderer {

  private PathPatternRenderer() {}

  static String render(PathPattern pathPattern) {
    StringBuilder cypher = new StringBuilder(renderNode(pathPattern.start()));
    for (Traversal traversal : pathPattern.traversals()) {
      RelationshipPattern relationship = traversal.relationship();
      String renderedRelationship = renderRelationship(relationship);
      switch (relationship.direction()) {
        case OUTGOING -> cypher.append("-").append(renderedRelationship).append("->");
        case INCOMING -> cypher.append("<-").append(renderedRelationship).append("-");
        case UNDIRECTED -> cypher.append("-").append(renderedRelationship).append("-");
        default ->
            throw new IllegalArgumentException(
                "unsupported relationship direction: " + relationship.direction());
      }
      cypher.append(renderNode(traversal.target()));
    }
    return cypher.toString();
  }

  private static String renderNode(NodePattern node) {
    return "(" + node.alias() + ":" + node.label() + ")";
  }

  private static String renderRelationship(RelationshipPattern relationship) {
    StringBuilder rendered = new StringBuilder("[");
    if (relationship.alias() != null) {
      rendered.append(relationship.alias());
    }
    rendered.append(":").append(relationship.type());
    if (relationship.variableLength()) {
      rendered.append("*").append(relationship.minimumHops()).append("..");
      if (relationship.maximumHops() != null) {
        rendered.append(relationship.maximumHops());
      }
    }
    return rendered.append("]").toString();
  }
}
