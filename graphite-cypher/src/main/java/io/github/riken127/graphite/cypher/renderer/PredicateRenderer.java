package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.predicate.ComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import io.github.riken127.graphite.core.model.predicate.NullPredicate;
import io.github.riken127.graphite.core.model.predicate.Predicate;
import io.github.riken127.graphite.core.model.predicate.TextPredicate;
import java.util.ArrayList;
import java.util.List;

final class PredicateRenderer {

  private PredicateRenderer() {}

  static String render(List<Predicate> predicates, ParameterAccumulator parameters) {
    List<String> parts = new ArrayList<>();

    for (Predicate predicate : predicates) {
      parts.add(renderPredicate(predicate, parameters));
    }

    return String.join(" AND ", parts);
  }

  private static String renderPredicate(Predicate predicate, ParameterAccumulator parameters) {
    if (predicate instanceof ComparisonPredicate comparisonPredicate) {
      return renderComparison(comparisonPredicate, parameters);
    }
    if (predicate instanceof InPredicate inPredicate) {
      return renderIn(inPredicate, parameters);
    }
    if (predicate instanceof TextPredicate textPredicate) {
      return renderText(textPredicate, parameters);
    }
    if (predicate instanceof NullPredicate nullPredicate) {
      return renderNull(nullPredicate);
    }

    throw new IllegalArgumentException(
        "unsupported predicate type: " + predicate.getClass().getSimpleName());
  }

  private static String renderComparison(
      ComparisonPredicate predicate, ParameterAccumulator parameters) {
    return predicate.alias()
        + "."
        + predicate.property()
        + " "
        + predicate.operator().cypherSymbol()
        + " "
        + parameters.add(predicate.value());
  }

  private static String renderIn(InPredicate predicate, ParameterAccumulator parameters) {
    return predicate.alias()
        + "."
        + predicate.property()
        + " IN "
        + parameters.add(predicate.values());
  }

  private static String renderText(TextPredicate predicate, ParameterAccumulator parameters) {
    return predicate.alias()
        + "."
        + predicate.property()
        + " "
        + predicate.operator().cypherKeyword()
        + " "
        + parameters.add(predicate.value());
  }

  private static String renderNull(NullPredicate predicate) {
    return predicate.alias()
        + "."
        + predicate.property()
        + (predicate.isNull() ? " IS NULL" : " IS NOT NULL");
  }
}
