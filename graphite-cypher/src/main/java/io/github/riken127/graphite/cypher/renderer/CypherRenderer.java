package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.Query;
import io.github.riken127.graphite.cypher.model.RawCypherQuery;
import io.github.riken127.graphite.cypher.model.RenderedQuery;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Unified, extensible facade for rendering Graphite query models. */
public final class CypherRenderer {

  private final Map<Class<? extends Query>, QueryRenderer<?>> renderers;

  /** Creates a renderer with all built-in query types. */
  public CypherRenderer() {
    this(
        List.of(
            new ClauseQueryRenderer(),
            new UnionQueryRenderer(),
            new MatchQueryRenderer(),
            new CreateQueryRenderer(),
            new MergeQueryRenderer(),
            new UpdateQueryRenderer(),
            new DeleteQueryRenderer()));
  }

  /** Creates a renderer with an explicit renderer collection. */
  public CypherRenderer(Collection<QueryRenderer<?>> queryRenderers) {
    Objects.requireNonNull(queryRenderers, "queryRenderers must not be null");
    Map<Class<? extends Query>, QueryRenderer<?>> registered = new LinkedHashMap<>();
    for (QueryRenderer<?> renderer : queryRenderers) {
      Objects.requireNonNull(renderer, "query renderer must not be null");
      if (registered.putIfAbsent(renderer.queryType(), renderer) != null) {
        throw new IllegalArgumentException(
            "duplicate renderer for query type: " + renderer.queryType().getName());
      }
    }
    this.renderers = Map.copyOf(registered);
  }

  /** Returns a new facade with an additional query renderer. */
  public CypherRenderer withRenderer(QueryRenderer<?> queryRenderer) {
    Objects.requireNonNull(queryRenderer, "queryRenderer must not be null");
    if (renderers.containsKey(queryRenderer.queryType())) {
      throw new IllegalArgumentException(
          "renderer already registered for query type: " + queryRenderer.queryType().getName());
    }
    List<QueryRenderer<?>> extended = new java.util.ArrayList<>(renderers.values());
    extended.add(queryRenderer);
    return new CypherRenderer(extended);
  }

  /** Renders a supported structured query. */
  public RenderedQuery render(Query query) {
    Objects.requireNonNull(query, "query must not be null");
    QueryRenderer<?> renderer = renderers.get(query.getClass());
    if (renderer == null) {
      throw new IllegalArgumentException(
          "no renderer registered for: " + query.getClass().getName());
    }
    return renderUnchecked(renderer, query);
  }

  /** Passes trusted raw Cypher through while preserving separate parameters. */
  public RenderedQuery render(RawCypherQuery query) {
    Objects.requireNonNull(query, "query must not be null");
    return new RenderedQuery(query.cypher(), query.parameters());
  }

  @SuppressWarnings("unchecked")
  private static <Q extends Query> RenderedQuery renderUnchecked(
      QueryRenderer<?> renderer, Query query) {
    return ((QueryRenderer<Q>) renderer).render((Q) query);
  }
}
