package prop.assignment0;

import java.util.Map;

// assign = id '=' expr ';'
public class AssignmentNode implements INode {
  private Lexeme id;
  private Lexeme assignOp;
  private ExpressionNode expr;
  private Lexeme semicolon;
  private Map<String, Double> environment;

  public AssignmentNode(Lexeme id, Lexeme assignOp, ExpressionNode expr,
      Lexeme semicolon, Map<String, Double> environment) {
    this.id = id;
    this.assignOp = assignOp;
    this.expr = expr;
    this.semicolon = semicolon;
    this.environment = environment;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    double value = (double) expr.evaluate(args);
    value = Math.round(value * 100.0) / 100.0; // trim float noise (e.g. -0.20000000000000007 -> -0.2)
    environment.put((String) id.value(), value);
    return ((String) id.value()) + " = " + value + "\n";
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("AssignmentNode\n");
    Util.addTabs(builder, tabs + 1);
    builder.append(id.toString()).append('\n');
    Util.addTabs(builder, tabs + 1);
    builder.append(assignOp.toString()).append('\n');
    expr.buildString(builder, tabs + 1);
    Util.addTabs(builder, tabs + 1);
    builder.append(semicolon.toString()).append('\n');
  }
}
