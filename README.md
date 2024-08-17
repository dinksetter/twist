# twist - Teeny Weenie Interpreted Scripting Tool

*twist* is a script and expression language interpreter that provides many of the same capabilities as more comprehensive scripting languages, but doesn't try to go overboard with complex capabilities. A few of the things you can do with twist:

- Evaluate complex expressions
- Set and reference variables
- Operate on JSON objects and arrays
- if/else
- try/catch/finally
- Engine-defined function
- Script-defined functions
- Get/Set properties on Java objects (using getters and setters)
- Call methods on Java objects
- for loops

## Getting Started
The three main classes involved in scripting are `TwistEngine`, `ScriptContext` and `EvalContext`. `TwistEngine` parses
scripts and expressions, producing `Script` and `Expression` objects. Those objects are evaluated
in a script or expression context.

```java
TwistEngine t = new TwistEngine();
ScriptContext exec = new SimpleScriptContext();
Script script = t.parseScript("a = 99 * 99");
script.execute(exec);
Integer value = (Integer) exec.getVariable("a");
```