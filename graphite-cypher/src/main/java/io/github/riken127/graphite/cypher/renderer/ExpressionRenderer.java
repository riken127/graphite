package io.github.riken127.graphite.cypher.renderer;

import io.github.riken127.graphite.core.dsl.TypedPropertyRef;
import io.github.riken127.graphite.core.model.expression.Expression;
import io.github.riken127.graphite.core.model.expression.FunctionExpression;
import io.github.riken127.graphite.core.model.expression.ParameterExpression;
import io.github.riken127.graphite.core.model.expression.Projection;
import io.github.riken127.graphite.core.model.expression.PropertyExpression;
import io.github.riken127.graphite.core.model.expression.VariableExpression;
import java.util.ArrayList;
import java.util.List;

final class ExpressionRenderer {

  private ExpressionRenderer() {}

  static String render(Expression<?> expression, ParameterAccumulator parameters) {
    if (expression instanceof VariableExpression<?> variable) {
      return variable.alias();
    }
    if (expression instanceof PropertyExpression<?> property) {
      return property.alias() + "." + property.property();
    }
    if (expression instanceof TypedPropertyRef<?> property) {
      return property.alias() + "." + property.property();
    }
    if (expression instanceof ParameterExpression<?> parameter) {
      return parameters.add(parameter.value());
    }
    if (expression instanceof FunctionExpression<?> function) {
      List<String> arguments = new ArrayList<>();
      for (Expression<?> argument : function.arguments()) {
        arguments.add(render(argument, parameters));
      }
      String prefix = function.distinct() ? "DISTINCT " : "";
      return function.name() + "(" + prefix + String.join(", ", arguments) + ")";
    }
    throw new IllegalArgumentException(
        "unsupported expression type: " + expression.getClass().getName());
  }

  static String renderProjection(Projection projection, ParameterAccumulator parameters) {
    String rendered = render(projection.expression(), parameters);
    return projection.alias() == null ? rendered : rendered + " AS " + projection.alias();
  }
}
