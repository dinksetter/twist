# twist - Teeny Weenie Interpreted Scripting Tool

*twist* is a script and expression language interpreter that provides many of the same capabilities as more comprehensive scripting languages, but doesn't try to go overboard with complex capabilities. A few of the things you can do with twist:

- Evaluate complex expressions
- Set and reference variables
- Operate on JSON objects and arrays
- if/else
- try/catch/finally
- Engine-defined function
- Script-defined functions
- Lambdas (anonymous functions)
- Get/Set properties on Java objects (using getters and setters)
- Call methods on Java objects
- for loops

## Installation

```xml
<dependency>
  <groupId>com.inksetter</groupId>
  <artifactId>twist</artifactId>
  <version>1.12.0</version>
</dependency>
```

twist requires Java 17 or later.

## Getting Started

### The quick way: `Twist`

The `Twist` class has static helpers for one-off evaluation:

```java
// Evaluate an expression against a map of variables
Object greeting = Twist.eval("'Hello, ' + name + '!'", Map.of("name", "World"));   // "Hello, World!"

// Evaluate and convert the result to a specific type
Boolean ok = Twist.eval("age >= 18 && country == 'US'",
        Map.of("age", 21, "country", "US"), Boolean.class);                        // true

// Execute a script. The value of the last statement (or of a return) is the result.
Object total = Twist.exec("""
        t = 0
        for (x : items) { t += x }
        t
        """, Map.of("items", List.of(1, 2, 3, 4)));                                 // 10
```

`Twist.exec` copies the map you pass in, so variables the script sets are not written back to your map. If you
need to read variables after the script runs, use a `ScriptContext` (see the next section).

### Parsing once, running many times

`TwistEngine` (or `Twist.parseScript` / `Twist.parseExpression`) parses scripts and expressions into `Script` and
`Expression` objects. You can run a parsed object any number of times against different contexts.

- `ScriptContext` (usually `SimpleScriptContext`) holds variables and functions for scripts.
- `EvalContext` (for example `MapContext`) is a simpler context for evaluating expressions.

```java
TwistEngine t = new TwistEngine();
ScriptContext exec = new SimpleScriptContext();
Script script = t.parseScript("a = 99 * 99");
script.execute(exec);
Integer value = (Integer) exec.getVariable("a");   // 9801
```

```java
Expression rule = Twist.parseExpression("order.total > 100 && order.region like 'EU%'");

for (Order order : orders) {
    MapContext ctx = new MapContext(Map.of("order", order));
    if (rule.evaluate(ctx, Boolean.class)) {
        // ...
    }
}
```

`evaluate(ctx, Class)` converts the result to `Boolean`, `String`, `Integer`, `Long`, `Double` or `Date`, or returns
it unchanged if it's already an instance of the requested class.

### Providing functions from Java

Functions are `TwistFunction` instances. `TwistFunction` is a functional interface, so a lambda works:

```java
Map<String, TwistFunction> functions = Map.of(
        "log", (args, ctx) -> { System.out.println(args); return null; },
        "lookup", (args, ctx) -> ctx.getVariable(String.valueOf(args.get(0))));

ScriptContext ctx = new SimpleScriptContext(Map.of("user", currentUser), functions);
Twist.parseScript("log('hello ' + user.name)").execute(ctx);
```

You can also add a function later with `ctx.addFunction(name, function)`.

### Resolving names your own way

Extend `SimpleScriptContext` to give a script a fallback for names it doesn't know — functions
looked up in a registry, variables fetched from a service, and so on:

```java
public class LookupContext extends SimpleScriptContext {
    @Override
    public TwistFunction lookupFunction(String name) {
        TwistFunction found = super.lookupFunction(name);
        return found != null ? found : myRegistry.get(name);
    }
}
```

Scripts run in nested scopes — a block, a loop, a function body, a lambda — and each is its own
frame. Those frames delegate back to the context you created, so an override of `getVariable`,
`isDefined`, `lookupFunction` or `addFunction` applies at any depth. Registered functions and real
variables are still found first; the override only sees what twist couldn't resolve.

Overriding `setVariable` or `getAll` catches only what happens at the top level of a script, because
a variable created inside a nested scope belongs to that frame rather than to yours.

### Functions that read their arguments as expressions

A function that implements `ExpressionFunction` receives its arguments unevaluated, as `Expression`
objects. That lets it see how an argument was written, or decide whether to evaluate it at all —
useful for building things like a testing DSL. `AssertThrowsFunction` is a working example:

```java
ctx.addFunction("assertThrows", new AssertThrowsFunction());
```

```
// Both forms run the code and check what it throws
assertThrows(NumberFormatException, -> { int('x') })
assertThrows(NumberFormatException, int('x'))

// The matching exception is returned, so you can check it further
e = assertThrows(NumberFormatException, -> { int('nope') })
e.getMessage() =~ 'nope'

// An exception thrown by a Java method is matched by its own type
assertThrows(StringIndexOutOfBoundsException, -> { 'abc'.substring(10) })
```

The exception type is written as a bare name, exactly as in a `catch` clause, and matched the same
way: by simple class name, including supertypes, against the exception and the causes it wraps. A
string or a `Class`-valued variable works in that position too. If nothing is thrown, or the wrong
type is, `assertThrows` throws a `TwistException` that a script can itself catch.

An implementation must evaluate each argument it uses exactly once:

```java
public class MyFunction implements ExpressionFunction {
    public Object invokeRaw(List<Expression> args, EvalContext ctx) throws TwistException {
        return args.get(0).evaluate(ctx);
    }
}
```

## Language Guide

### Statements and comments

A statement ends with a semicolon or a newline. To put more than one statement on a line, separate them with
semicolons.

```
a = 100
b = a + 4; c = b * 2

// single-line comment
/*
 * multi-line comment
 */
```

A script's result is the value of the last statement it runs, unless it runs a `return` first.

### Literals

```
42                  // Integer
3000000000          // Double: whole numbers outside the int range become Double
3.14   1e3          // Double
'single'  "double"  // String
'it''s'             // double the quote character to escape it: it's
true  false  null
[1, 2, 'three']     // List
{name: 'twist', 'first version': 1.0, tags: ['a', 'b']}   // Map; keys can be identifiers or strings
```

twist does not support backslash escapes. A string can contain a literal newline, or you can use a triple-quoted
string. When a triple-quoted string starts with a newline, the indentation of its first line is removed from every
line:

```
text = """
    line one
    line two
    """
// text == "line one\nline two\n"
```

### Operators

| Category   | Operators                                                  |
|------------|------------------------------------------------------------|
| Arithmetic | `+` `-` `*` `/` `%`                                        |
| Comparison | `==` `!=` `<>` `<` `<=` `>` `>=`                           |
| Pattern    | `like` `not like` `=~` `==~` `!~`                          |
| Logical    | `&&` `\|\|` `!`                                            |
| Other      | `cond ? a : b`, `a ?: b` (elvis)                           |
| Assignment | `=` `+=` `-=` `*=` `/=` `%=` `++` `--` (postfix)           |

```
7 / 2                    // 3   (integer division when both sides are integers)
7 / 2.0                  // 3.5
17 % 5                   // 2
'Hello, ' + name         // string concatenation when the left side is a string
'10' * 2                 // 20  (strings are converted to numbers for arithmetic)
'1' == 1                 // true

n > 5 ? 'big' : 'small'
nickname ?: 'anonymous'  // right side is used when the left side is null or ""

a = b = c = 0            // assignments are expressions and can be chained
count++
total += price * qty
```

When the left side of `+` is a string, the right side is converted to a string. Otherwise both sides are treated as
numbers, so `1 + 'a'` is an error. Put the string first, or use `string()`.

A `-` sign in front of a value negates it, whether it's a literal or an expression (`-3`, `-x`, `-items[0]`).
Negating null gives null.

#### Pattern matching

```
name like 'W%'           // SQL-style: % matches any run of characters, _ matches exactly one
name like 'W_rld'
name not like 'X%'
name =~ 'orl'            // regex: true if the pattern is found anywhere
name ==~ 'W.*d'          // regex: true only if the whole string matches
name !~ '[0-9]'          // regex: true if the whole string does NOT match
```

### Variables and data

Variables don't need to be declared. A variable that was never set evaluates to `null`.

```
config = {
    db: { host: 'localhost', ports: [5432, 5433] },
    debug: false
}

config.db.ports[1]           // 5433
config.db.host = 'db.local'  // set a map entry
config['db']['host']         // the same entry, by key
config.missing               // null

list = ['a', 'b', 'c']
list[0] = 'z'                // set a list (or Java array) element
```

A `{` at the very start of a statement opens a block, not a map. To start a statement with a map literal, assign
it or `return` it.

Use `.` or `[...]` to get and set values in maps. `[...]` takes the key as it is written, so `m['a b']` works
for keys that aren't identifiers, and `m[k]` looks up whatever `k` holds. Lists and Java arrays are indexed by
position, starting at 0.

A line that starts with `(` or `[` continues the expression on the line before it. For example,
`x = a` followed by a line `(b + 1)` is parsed as the call `x = a(b + 1)`. End the earlier statement with `;` if
you need to start a line that way.

### Java objects

twist uses JavaBean getters and setters for properties, and it supports records:

```java
public class Person {
    public String getName() { ... }
    public void setName(String name) { ... }
}
```

```
person.name                          // calls getName()
person.name = 'Alice'                // calls setName("Alice")
person.name.toUpperCase().substring(0, 3)   // calls Java methods, including chained calls
'a,b,c'.split(',')[1]                // "b"
```

If an object implements `MethodInterceptor`, twist sends every method call on it to
`invokeMethod(name, args)`. You can use this to build dynamic objects in Java.

### Conditionals

```
if (score >= 90) grade = 'A'
else if (score >= 80) grade = 'B'
else grade = 'C'

if (user.active && user.role == 'admin') {
    allowed = true
} else {
    allowed = false
}
```

### Loops

```
// C-style
total = 0
for (i = 1; i <= 10; i++) {
    total += i
}

// for-each: works on any List, Java array or other Iterable
names = ''
for (n : ['ann', 'bob', 'cy']) {
    names += upper(n) + ' '
}
```

`return` inside a loop ends the loop and the enclosing function or script. twist has no `break`, `continue` or
`while`.

### Scope

A `{ ... }` block (including the body of an `if` or `for`) creates a new scope. Assigning to a variable that
already exists in an outer scope updates it. Assigning to a new variable creates it in the current block, and it
disappears when the block ends:

```
found = false
for (x : items) {
    if (x > 100) { found = true }   // updates the outer 'found'
    tmp = x * 2                     // local to the loop body
}
// found is updated; tmp is null here
```

A `for-each` loop variable belongs to the loop. A C-style loop variable like `i` stays set after the loop.

Each scope is a frame that refers to the one enclosing it. A `ScriptContext` *is* one of those frames: `push()`
returns a nested frame for a block, and `pushCall()` returns one for a function body, where assignment stops
rather than reaching the enclosing scope. Nothing needs to be popped.

### Functions

Use `def` to define a named function. It returns the value of its last statement, or of a `return`.

```
def fact(n) {
    if (n <= 1) return 1
    return n * fact(n - 1)
}

fact(10)   // 3628800
```

A function sees its arguments, the variables it creates, and the scope it was **defined** in — not the scope it
was called from, so it can't see the caller's local variables. Assigning to a variable inside a `def` function
creates a local variable, even if an enclosing scope has the same name:

```
x = 1
def f() { x = 2; x }
[f(), x]   // [2, 1]
```

### Lambdas

`->` creates an anonymous function value. You can store it in a variable, a list or a map, pass it to another
function, or call it right away:

```
twice = -> (x) { x * 2 }
greet  = -> { 'hi' }                  // no parameter list is needed when there are no arguments

twice(21)                             // 42
greet()                               // "hi"

apply = -> (f, v) { f(v) }
apply(-> (x) { x * 10 }, 4)           // 40

handlers = [-> (x) { x + 1 }]
handlers[0](1)                        // 2

(-> (a, b) { a + b })(1, 2)           // 3
```

A call can be applied to anything that evaluates to a function, anywhere in an expression: `fs[0](1) + 2`,
`m['f'](3)` and `add(1)(2)` all work.

#### Lambdas are closures

A lambda captures the scope it was created in and keeps it, even when it's called somewhere else entirely. Unlike
a `def` function, it *shares* that scope rather than getting a private copy, so assigning to a captured name
updates the original:

```
count = 0
bump = -> { count += 1 }
bump(); bump()
count                                 // 2

def mk(n) { return -> (x) { x + n } }
add5 = mk(5)
add5(1)                               // 6, and it still works after mk() has returned
```

Arguments and brand-new variables still belong to the lambda, so they never overwrite a captured name:

```
x = 1
f = -> (x) { x * 10 }
[f(2), x]                             // [20, 1] — the parameter shadows the captured x
```

A loop has one scope for the whole loop rather than one per pass, so lambdas created inside a loop all capture
the same variable and see its final value:

```
fs = []
for (i : [1, 2, 3]) { fs.add(-> { i }) }
[fs[0](), fs[1](), fs[2]()]           // [3, 3, 3]
```

Also, `obj.f(x)` calls a *Java method* named `f`. It does not call a lambda stored in a map; fetch it first
(`g = obj.f; g(x)`) or index it (`obj['f'](x)`).

### Error handling

```
n = null                  // declare it outside the try, or it disappears when the block ends
try {
    n = int(input)
}
catch (NumberFormatException e) {
    n = 0
    log('bad input: ' + e.getMessage())   // log() is a function provided from Java
}
catch (Exception e) {
    n = -1
}
finally {
    log('done')
}
```

A catch clause matches by the exception's simple class name, and it also matches superclasses, so
`catch (Exception e)` catches everything. An exception thrown by a Java method is matched by its own type, such as
`catch (StringIndexOutOfBoundsException e)`. The variable (`e`) holds the Java exception object. If no clause
matches, the exception keeps propagating after the `finally` block runs. twist has no `throw` statement.

### Built-in functions

Built-in function names are case-insensitive. Built-ins are checked first, so a variable or `def` with the same
name as a built-in (such as `double` or `max`) can't be called.

| Function                         | Description                                                           |
|----------------------------------|-----------------------------------------------------------------------|
| `string(x)`                      | Convert to string                                                     |
| `int(x)`, `double(x)`            | Convert to a number. Throws `NumberFormatException` for bad input     |
| `date(x)`                        | Parse an ISO-8601 date/time string (for example `'2024-05-06T07:03:09Z'`) |
| `now()`                          | The current date/time                                                 |
| `upper(s)`, `lower(s)`, `trim(s)`| String case and whitespace                                            |
| `len(s)` / `length(s)`           | String length. `null` gives 0                                         |
| `substr(s, start [, length])`    | Substring with a **1-based** start. A negative start counts from the end |
| `indexof(s, search [, start])`   | **1-based** position of `search`, or 0 if it isn't found              |
| `sprintf(fmt, args...)`          | Java `String.format`                                                  |
| `min(a, b, ...)`, `max(a, b, ...)`| Smallest or largest argument                                         |
| `type(x)`                        | Type name: `STRING`, `INTEGER`, `DOUBLE`, `BOOLEAN`, `DATETIME`, `ARRAY`, `OBJECT`, ... |
| `json(x [, pretty])`             | Render a value as JSON text, escaping strings                         |
| `eval(s)`                        | Parse and evaluate a string as an expression (useful for parsing JSON text) |
| `b64encode(bytes)`, `b64decode(s)`| Base64 encoding and decoding (`byte[]` <-> string)                   |

```
substr('World', 2, 3)                    // "orl"
substr('World', -3)                      // "rld"
indexof('World', 'r')                    // 3
sprintf('%s has %d items', name, 3)
json({a: 1, b: [2, 3]})                  // {"a":1,"b":[2,3]}
json({ok: true, note: 'say "hi"'})       // {"ok":true,"note":"say \"hi\""}
eval('{"a": [1, 2]}').a[1]               // 2
```

#### Dates

You can add days to a date, or subtract days from it. Use a fractional number for part of a day:

```
tomorrow = now() + 1
sixHoursAgo = now() - 0.25
```

Dates compare with the usual operators, and `string()` renders one in ISO-8601 form
(`2024-01-01T00:00:00Z`). Subtracting one date from another gives the difference in days, as a Double. The
result is positive when the left date is later:

```
date('2024-01-03T00:00:00Z') - date('2024-01-01T00:00:00Z')   // 2.0
date('2024-01-01T00:00:00Z') - date('2024-01-01T12:00:00Z')   // -0.5

expiry < now()                                                // true once expiry has passed
'expires ' + expiry                                           // "expires 2024-01-01T00:00:00Z"
```

## A Larger Example

```
orders = eval(ordersJson)

def lineTotal(line) {
    return line.qty * line.price
}

summary = {count: 0, revenue: 0.0, largest: 0.0, bigOrders: ''}

for (o : orders) {
    t = 0.0
    for (line : o.lines) { t += lineTotal(line) }

    summary.count += 1
    summary.revenue += t
    summary.largest = max(summary.largest, t)
    if (t > 1000) {
        summary.bigOrders = trim(summary.bigOrders + ' ' + o.id)
    }
}

json(summary, true)
```
