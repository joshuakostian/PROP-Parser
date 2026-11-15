package prop.assignment0;

// stmts = [ assign stmts ]
public class StatementsNode implements INode {
  private AssignmentNode assignment; // null = empty (epsilon)
  private StatementsNode statements;

  public StatementsNode(AssignmentNode assignment, StatementsNode statements) {
    this.assignment = assignment;
    this.statements = statements;
  }

  @Override
  public Object evaluate(Object[] args) throws Exception {
    if (assignment == null)
      return "";
    return assignment.evaluate(args).toString() + statements.evaluate(args).toString();
  }

  @Override
  public void buildString(StringBuilder builder, int tabs) {
    Util.addTabs(builder, tabs);
    builder.append("StatementsNode\n");
    if (assignment != null) {
      assignment.buildString(builder, tabs + 1);
      statements.buildString(builder, tabs + 1);
    }
  }
}
