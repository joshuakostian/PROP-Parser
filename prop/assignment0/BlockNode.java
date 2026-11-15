package prop.assignment0;

// block = '{' stmts '}'
public class BlockNode implements INode {
  private Lexeme leftCurly;
  private StatementsNode statements;
  private Lexeme rightCurly;

  public BlockNode(Lexeme leftCurly, StatementsNode statements, Lexeme rightCurly) {
    this.leftCurly = leftCurly;
    this.statements = statements;
    this.rightCurly = rightCurly;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    return statements.evaluate(args);
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("BlockNode\n");

    // Braces print at the SAME indent as "BlockNode" (see parsetree2.txt).
    Util.addTabs(builder, tabs);
    builder.append(leftCurly.toString()).append('\n');

    statements.buildString(builder, tabs + 1);

    Util.addTabs(builder, tabs);
    builder.append(rightCurly.toString()).append('\n');
  }
}
