# How the evaluator works

The evaluator is the `evaluate(Object[] args)` method that every node implements (from `INode`). `Program.main` calls it once on the root, and it recurses down the tree: each node computes its own value from its children's values. This is the "distributed OO evaluator" the assignment asks for.

## Two things that make evaluation non-obvious

1. **Nodes return two different types.** `FactorNode`, `TermNode` and `ExpressionNode` produce a `Double`; `AssignmentNode`, `StatementsNode` and `BlockNode` produce a `String`. The `Object` return type is the "could be either" escape hatch.

2. **The tree leans right, but the operators are left-associative.** This is the hard part, and most of this document is about it.

---

## 1. Evaluation is bottom-up: leaves first, then parents

Every node's `evaluate` answers *"what do I compute from my children's values?"*. Leaves are the base case; the answers bubble up to the root.

For `a = 2 + 3;`:

```
AssignmentNode.evaluate
└── expr.evaluate()
    └── ExpressionNode(+)
        ├── term.evaluate()  → 2.0
        └── ...              → 3.0
        → returns 2.0 + 3.0 = 5.0

back in AssignmentNode:
   value = 5.0
   symbolTable["a"] = 5.0
   return "a = 5.0\n"
```

`AssignmentNode` asks its `ExpressionNode`, which asks its `Term`s, which ask their `Factor`s, which return literal values — and the answers bubble back up.

---

## 2. A right-leaning tree vs. left-associative operators

The grammar is right-recursive:

```
expr = term [ ('+' | '-') expr ]    ← the recursion hangs off the RIGHT
```

This avoids left-recursion (which would infinite-loop a recursive-descent parser), but it produces a tree that **leans right**. For `10 - 3 - 2`:

```
ExpressionNode( 10, -, ExpressionNode( 3, -, ExpressionNode(2) ) )
```

Naively reading that right-to-left gives `10 - (3 - 2)`. A naive evaluator — "compute my whole right child, then combine" — would give the wrong answer:

```
10 - (3 - 2) = 10 - 1 = 9     ← WRONG (right-associative)
```

But subtraction is **left-associative**: `(10 - 3) - 2 = 5`.

So `evaluateFromLeft` exists to walk a tree that leans right while doing math that associates left.

---

## 3. The fix: thread an accumulator

The load-bearing part of `ExpressionNode` (and `TermNode`, identically but for `*`/`/`):

```java
private double evaluateFromLeft(Object[] args, double acc) {
    if (op == null)
        return acc;                                     // no more operators → done

    double next = (double) expr.term().evaluate(args);  // the REST's *first* term only
    double result = (op.token() == Token.ADD_OP) ? acc + next : acc - next;
    return expr.evaluateFromLeft(args, result);         // recurse with the new acc
}
```

**What `acc` means:** *the value of everything seen so far, moving left to right.*

The crucial line is `expr.term()` — it grabs **only the first term of the rest**, not the whole rest. That's the entire trick.

| Strategy | Combine with | Result |
|---|---|---|
| ❌ right-associative | `acc op expr.evaluate()` (the whole rest) | `10 - (3 - 2)` = 9 |
| ✅ left-associative | `acc op expr.term()` (one term), then continue | `(10 - 3) - 2` = 5 |

Tracing `10 - 3 - 2` (tree `E1(10, -, E2(3, -, E3(2)))`):

| Step | `acc` | this node's op | `next` (first term of the rest) | new `acc` |
|---|---|---|---|---|
| start | `10.0` | `-` | `3.0` | `10.0 - 3.0 = 7.0` |
| recurse | `7.0` | `-` | `2.0` | `7.0 - 2.0 = 5.0` |
| recurse | `5.0` | *(none)* | — | return `5.0` |

This is exactly the same as flattening the operator chain into a list and folding from the left — the accumulator just does that fold while walking the recursive tree.

---

## 4. Precedence falls out of the layering

The accumulator walks left-to-right, but precedence is still correct because the two levels are separate:

- `ExpressionNode` folds `+` / `-` one **term** at a time.
- `TermNode` folds `*` / `/` one **factor** at a time.

For `10 - 2 * 3`:

```
ExpressionNode( 10, -, ExpressionNode( Term( 2 * 3 ) ) )
```

`Term(2 * 3)` is a single term — its own `TermNode.evaluate` computes `2 * 3 = 6.0` *before* the `-` sees it:

```
E.evaluate → evaluateFromLeft(acc = 10.0)
   next = E.term().evaluate() = Term(2*3).evaluate() = 6.0   ← '*' already done
   result = 10.0 - 6.0 = 4.0
```

`10 - (2 * 3) = 4`, not `(10 - 2) * 3 = 24`. Precedence is guaranteed by *which layer* consumes which operator — `*`/`/` are parsed by `parseTerm` (a tighter layer), `+`/`-` by `parseExpr`.

---

## 5. The type boundary and the symbol table

**`FactorNode`** — the leaf, returns a `Double`:

```java
if (operand.token() == Token.INT_LIT)
    return operand.value();                                       // literal
return symbolTable.getOrDefault((String) operand.value(), 0.0);   // variable → value (or 0.0)
```

A variable like `a` evaluates to its current value in the shared map, defaulting to `0.0` if never assigned (the spec's rule).

**`AssignmentNode`** — the boundary, returns a `String`:

```java
double value = (double) expr.evaluate(args);                      // get a Double
value = Math.round(value * 100.0) / 100.0;                       // clean float noise
symbolTable.put((String) id.value(), value);                     // STORE the variable
return ((String) id.value()) + " = " + value + "\n";             // "a = 5.0\n"
```

This is the one place a `Double` turns into text: it stores the value, then returns the human-readable line.

**`StatementsNode` / `BlockNode`** — just concatenate those lines:

```java
return assignment.evaluate(args).toString() + statements.evaluate(args).toString();
```

Put together, for `{ a = 2 + 3; b = a * 4; }`:

```
BlockNode.evaluate
└── StatementsNode.evaluate
    ├── AssignmentNode(a) → expr 5.0,  symbolTable["a"] = 5.0,  "a = 5.0\n"
    └── StatementsNode.evaluate
        └── AssignmentNode(b) → expr a*4 = 5.0*4.0 = 20.0,  symbolTable["b"] = 20.0,  "b = 20.0\n"
→ "a = 5.0\nb = 20.0\n"
```

The second statement reads what the first stored — that's the shared symbol table at work (and why `c = b + a` worked in `program2.txt`).

---

## Summary

| Node | Computes | Returns |
|---|---|---|
| `FactorNode` | literal value, or `symbolTable.get(id)` (default `0.0`), or parenthesized sub-expr | `Double` |
| `TermNode` | fold `*` `/` left-associatively across factors | `Double` |
| `ExpressionNode` | fold `+` `-` left-associatively across terms | `Double` |
| `AssignmentNode` | store variable, format `"name = value\n"` | `String` |
| `StatementsNode` / `BlockNode` | concatenate assignment strings | `String` |

The single hardest line in the whole evaluator:

```java
double next = (double) expr.term().evaluate(args);   // ONE term, not the whole rest
```

Combining with the *next* term and recursing — rather than recursing first and combining later — is what turns a right-leaning tree into left-associative math.
