package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RenderedQuery;

/** Renderer extension point for a specific query model. */
public interface QueryRenderer<Q extends Query> {

  /** Query type handled by this renderer. */
  Class<Q> queryType();

  /** Renders a query into Cypher and parameters. */
  RenderedQuery render(Q query);
}
