package prop.assignment0;

import java.util.Map;

// factor = int | id | '(' expr ')'
public class FactorNode implements INode {
  private Lexeme operand; // INT_LIT or IDENT (null for '(' expr ')')
  private Lexeme leftParen;
  private Lexeme rightParen;
  private ExpressionNode expr;
  private Map<String, Double> environment;

  public FactorNode(Lexeme operand, Map<String, Double> environment) {
    this.operand = operand;
    this.environment = environment;
  }

  public FactorNode(Lexeme leftParen, ExpressionNode expr, Lexeme rightParen,
      Map<String, Double> environment) {
    this.leftParen = leftParen;
    this.expr = expr;
    this.rightParen = rightParen;
    this.environment = environment;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    if (operand != null) {
      if (operand.token() == Token.INT_LIT)
        return operand.value();
      return environment.getOrDefault((String) operand.value(), 0.0); // unassigned id = 0
    }
    return expr.evaluate(args);
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("FactorNode\n");
    if (operand != null) {
      Util.addTabs(builder, tabs + 1);
      builder.append(operand.toString()).append('\n');
    } else {
      Util.addTabs(builder, tabs + 1);
      builder.append(leftParen.toString()).append('\n');
      expr.buildString(builder, tabs + 1);
      Util.addTabs(builder, tabs + 1);
      builder.append(rightParen.toString()).append('\n');
    }
  }
}
