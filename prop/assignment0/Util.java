package prop.assignment0;

final class Util {
  private Util() {
  }

  // Appends `tabs` tab characters.
  static void addTabs(StringBuilder builder, int tabs) {
    for (int i = 0; i < tabs; i++)
      builder.append('\t');
  }
}
