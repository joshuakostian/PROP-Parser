package prop.assignment0;

// term = factor [ ('*' | '/') term ]
public class TermNode implements INode {
  private FactorNode factor;
  private Lexeme op; // MULT_OP or DIV_OP, or null
  private TermNode term;

  public TermNode(FactorNode factor, Lexeme op, TermNode term) {
    this.factor = factor;
    this.op = op;
    this.term = term;
  }

  FactorNode factor() {
    return factor;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    return evaluateFromLeft(args, (double) factor.evaluate(args));
  }

  // Folds '*'/'/' left-associatively across the right-recursive chain.
  private double evaluateFromLeft(Object[] args, double acc) throws Exception {
    if (op == null)
      return acc;
    double next = (double) term.factor().evaluate(args);
    double result = (op.token() == Token.MULT_OP) ? acc * next : acc / next;
    return term.evaluateFromLeft(args, result);
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("TermNode\n");
    factor.buildString(builder, tabs + 1);
    if (op != null) {
      Util.addTabs(builder, tabs + 1);
      builder.append(op.toString()).append('\n');
      term.buildString(builder, tabs + 1);
    }
  }
}
