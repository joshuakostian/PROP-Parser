package prop.assignment0;

// expr = term [ ('+' | '-') expr ]
public class ExpressionNode implements INode {
  private TermNode term;
  private Lexeme op; // ADD_OP or SUB_OP or null
  private ExpressionNode expr;

  public ExpressionNode(TermNode term, Lexeme op, ExpressionNode expr) {
    this.term = term;
    this.op = op;
    this.expr = expr;
  }

  TermNode term() {
    return term;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    return evaluateFromLeft(args, (double) term.evaluate(args));
  }

  private double evaluateFromLeft(Object[] args, double valueSoFar) throws Exception {
    if (op == null)
      return valueSoFar;
    double next = (double) expr.term().evaluate(args);
    double result = (op.token() == Token.ADD_OP) ? valueSoFar + next : valueSoFar - next;
    return expr.evaluateFromLeft(args, result);
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("ExpressionNode\n");
    term.buildString(builder, tabs + 1);
    if (op != null) {
      Util.addTabs(builder, tabs + 1);
      builder.append(op.toString()).append('\n');
      expr.buildString(builder, tabs + 1);
    }
  }
}
