package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.model.predicate.ComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionComparisonPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionInPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionNullPredicate;
import io.github.riken127.graphite.core.model.predicate.ExpressionTextPredicate;
import io.github.riken127.graphite.core.model.predicate.InPredicate;
import io.github.riken127.graphite.core.model.predicate.LogicalPredicate;
import io.github.riken127.graphite.core.model.predicate.NotPredicate;
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
    if (predicate instanceof ExpressionComparisonPredicate comparisonPredicate) {
      return ExpressionRenderer.render(comparisonPredicate.left(), parameters)
          + " "
          + comparisonPredicate.operator().cypherSymbol()
          + " "
          + ExpressionRenderer.render(comparisonPredicate.right(), parameters);
    }
    if (predicate instanceof ExpressionInPredicate inPredicate) {
      return ExpressionRenderer.render(inPredicate.value(), parameters)
          + " IN "
          + ExpressionRenderer.render(inPredicate.collection(), parameters);
    }
    if (predicate instanceof ExpressionTextPredicate textPredicate) {
      return ExpressionRenderer.render(textPredicate.value(), parameters)
          + " "
          + textPredicate.operator().cypherKeyword()
          + " "
          + ExpressionRenderer.render(textPredicate.expected(), parameters);
    }
    if (predicate instanceof ExpressionNullPredicate nullPredicate) {
      return ExpressionRenderer.render(nullPredicate.expression(), parameters)
          + (nullPredicate.isNull() ? " IS NULL" : " IS NOT NULL");
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
    if (predicate instanceof LogicalPredicate logicalPredicate) {
      return "("
          + renderWithOperator(
              logicalPredicate.predicates(), logicalPredicate.operator().name(), parameters)
          + ")";
    }
    if (predicate instanceof NotPredicate notPredicate) {
      return "NOT (" + renderPredicate(notPredicate.predicate(), parameters) + ")";
    }

    throw new IllegalArgumentException(
        "unsupported predicate type: " + predicate.getClass().getSimpleName());
  }

  private static String renderWithOperator(
      List<Predicate> predicates, String operator, ParameterAccumulator parameters) {
    List<String> parts = new ArrayList<>();
    for (Predicate predicate : predicates) {
      parts.add(renderPredicate(predicate, parameters));
    }
    return String.join(" " + operator + " ", parts);
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
