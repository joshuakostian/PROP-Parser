# Assignment 0 — Tokenizer & Parser

A Java interpreter for a small assignment language (Grammar 2 from
`assignment0.pdf`): statement blocks and assignment statements with
left-associative arithmetic (`+ - * /`).

## Build

```bash
javac -d out prop/assignment0/*.java
```

Compiled `.class` files go into `out/` (gitignored).

## Run

```bash
java -cp out prop.assignment0.Program <input-file> <output-file>
```

For example:

```bash
java -cp out prop.assignment0.Program program1.txt result1.txt
java -cp out prop.assignment0.Program program2.txt result2.txt
```

The program prints tokenizer debug lines (`Lexeme:...`) to the console (from
the provided `Lexeme` class) and writes the parse tree and evaluation to the
output file.

## Verify against the expected outputs

```bash
diff parsetree1.txt result1.txt
diff parsetree2.txt result2.txt
```
