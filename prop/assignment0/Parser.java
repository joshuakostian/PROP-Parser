package prop.assignment0;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Recursive-descent parser for Grammar 2.
public class Parser implements IParser {
  private Tokenizer tokenizer = null;
  private Map<String, Double> environment = null; // shared symbol table

  public Parser() {
  }

  @Override
  public void open(String fileName) throws IOException, TokenizerException {
    tokenizer = new Tokenizer();
    tokenizer.open(fileName);
    environment = new HashMap<>();
  }

  @Override
  public INode parse() throws IOException, TokenizerException, ParserException {
    Lexeme token = tokenizer.current();
    if (token.token() == Token.LEFT_CURLY)
      return parseBlock();
    return parseAssignment();
  }

  @Override
  public void close() throws IOException {
    tokenizer.close();
  }

  private BlockNode parseBlock() throws IOException, TokenizerException, ParserException {
    Lexeme leftCurly = expect(Token.LEFT_CURLY);
    StatementsNode statements = parseStmts();
    Lexeme rightCurly = expect(Token.RIGHT_CURLY);
    return new BlockNode(leftCurly, statements, rightCurly);
  }

  private StatementsNode parseStmts() throws IOException, TokenizerException, ParserException {
    if (tokenizer.current().token() != Token.IDENT)
      return new StatementsNode(null, null); // empty (epsilon) list
    AssignmentNode assignment = parseAssignment();
    return new StatementsNode(assignment, parseStmts());
  }

  private AssignmentNode parseAssignment() throws IOException, TokenizerException, ParserException {
    Lexeme id = expect(Token.IDENT);
    Lexeme assignOp = expect(Token.ASSIGN_OP);
    ExpressionNode expr = parseExpr();
    Lexeme semicolon = expect(Token.SEMICOLON);
    return new AssignmentNode(id, assignOp, expr, semicolon, environment);
  }

  private ExpressionNode parseExpr() throws IOException, TokenizerException, ParserException {
    TermNode term = parseTerm();
    Lexeme token = tokenizer.current();
    if (token.token() == Token.ADD_OP || token.token() == Token.SUB_OP) {
      tokenizer.moveNext();
      return new ExpressionNode(term, token, parseExpr());
    }
    return new ExpressionNode(term, null, null);
  }

  private TermNode parseTerm() throws IOException, TokenizerException, ParserException {
    FactorNode factor = parseFactor();
    Lexeme token = tokenizer.current();
    if (token.token() == Token.MULT_OP || token.token() == Token.DIV_OP) {
      tokenizer.moveNext();
      return new TermNode(factor, token, parseTerm());
    }
    return new TermNode(factor, null, null);
  }

  private FactorNode parseFactor() throws IOException, TokenizerException, ParserException {
    Lexeme token = tokenizer.current();
    if (token.token() == Token.INT_LIT || token.token() == Token.IDENT) {
      tokenizer.moveNext();
      return new FactorNode(token, environment);
    }
    if (token.token() == Token.LEFT_PAREN) {
      tokenizer.moveNext();
      ExpressionNode expr = parseExpr();
      Lexeme rightParen = expect(Token.RIGHT_PAREN);
      return new FactorNode(token, expr, rightParen, environment);
    }
    throw new ParserException("Expected a factor (int, id or '(') but found " + token.token());
  }

  private Lexeme expect(Token expected) throws IOException, TokenizerException, ParserException {
    Lexeme lexeme = tokenizer.current();
    if (lexeme.token() != expected)
      throw new ParserException("Expected " + expected + " but found " + lexeme.token());
    tokenizer.moveNext();
    return lexeme;
  }
}
