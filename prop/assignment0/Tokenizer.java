package prop.assignment0;

import java.io.IOException;

public class Tokenizer implements ITokenizer {
  private Scanner scanner = null;
  private Lexeme current = null;

  public Tokenizer() {
  }

  @Override
  public void open(String fileName) throws IOException, TokenizerException {
    scanner = new Scanner();
    scanner.open(fileName);
    scanner.moveNext(); // first char
    moveNext(); // first lexeme
  }

  @Override
  public Lexeme current() {
    return current;
  }

  @Override
  public void moveNext() throws IOException, TokenizerException {
    current = readNextLexeme();
  }

  @Override
  public void close() throws IOException {
    if (scanner != null)
      scanner.close();
  }

  private Lexeme readNextLexeme() throws IOException, TokenizerException {
    skipWhitespace();

    char c = scanner.current();

    if (c == Scanner.EOF)
      return new Lexeme(null, Token.EOF);

    if (isIdentifierChar(c))
      return readIdentifier();

    if (isDigit(c))
      return readInteger();

    Token token;
    switch (c) {
      case '+':
        token = Token.ADD_OP;
        break;
      case '-':
        token = Token.SUB_OP;
        break;
      case '*':
        token = Token.MULT_OP;
        break;
      case '/':
        token = Token.DIV_OP;
        break;
      case '=':
        token = Token.ASSIGN_OP;
        break;
      case '(':
        token = Token.LEFT_PAREN;
        break;
      case ')':
        token = Token.RIGHT_PAREN;
        break;
      case ';':
        token = Token.SEMICOLON;
        break;
      case '{':
        token = Token.LEFT_CURLY;
        break;
      case '}':
        token = Token.RIGHT_CURLY;
        break;
      default:
        throw new TokenizerException("Unexpected character: '" + c + "'");
    }

    String value = Character.toString(c);
    scanner.moveNext();
    return new Lexeme(value, token);
  }

  private void skipWhitespace() throws IOException {
    while (Character.isWhitespace(scanner.current()))
      scanner.moveNext();
  }

  private Lexeme readIdentifier() throws IOException {
    StringBuilder sb = new StringBuilder();
    while (isIdentifierChar(scanner.current())) {
      sb.append(scanner.current());
      scanner.moveNext();
    }
    return new Lexeme(sb.toString(), Token.IDENT);
  }

  private Lexeme readInteger() throws IOException, TokenizerException {
    if (scanner.current() == '0')
      throw new TokenizerException("Invalid integer literal: '0' (numbers must start with 1-9)");

    StringBuilder sb = new StringBuilder();
    while (isDigit(scanner.current())) {
      sb.append(scanner.current());
      scanner.moveNext();
    }
    return new Lexeme(Double.parseDouble(sb.toString()), Token.INT_LIT);
  }

  private boolean isIdentifierChar(char c) {
    return c >= 'a' && c <= 'z';
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
}
