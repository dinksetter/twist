package com.inksetter.twist;

import com.inksetter.twist.expression.function.FunctionArgumentException;
import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.parser.ScriptSyntaxException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for script-defined functions (def) and lambdas.
 */
public class FunctionTest {

    private static Object exec(String script) throws TwistException {
        return Twist.exec(script, new HashMap<>());
    }

    @Test
    public void testRecursion() throws TwistException {
        assertEquals(3628800, exec("""
                def fact(n) {
                    if (n <= 1) return 1
                    return n * fact(n - 1)
                }
                fact(10)
                """));
        assertEquals(55, exec("def fib(n) { if (n < 2) return n; fib(n - 1) + fib(n - 2) }; fib(10)"));
    }

    @Test
    public void testNoArgFunction() throws TwistException {
        assertEquals("hi", exec("def greet() { 'hi' }; greet()"));
    }

    @Test
    public void testFunctionValueIsLastStatement() throws TwistException {
        assertEquals(3, exec("def f() { 1; 2; 3 }; f()"));
        assertNull(exec("def f() { }; f()"));
    }

    @Test
    public void testWrongArgumentCount() {
        assertThrows(FunctionArgumentException.class, () -> exec("def f(a) { a }; f(1, 2)"));
        assertThrows(FunctionArgumentException.class, () -> exec("def f(a, b) { a }; f(1)"));
        assertThrows(FunctionArgumentException.class, () -> exec("f = -> (a) { a }; f()"));
    }

    @Test
    public void testFunctionCanBeRedefined() throws TwistException {
        assertEquals(List.of(1, 2), exec("def f() { 1 }; a = f(); def f() { 2 }; [a, f()]"));
    }

    @Test
    public void testFunctionMustBeDefinedBeforeUse() {
        assertThrows(TwistException.class, () -> exec("f(); def f() { 1 }"));
    }

    @Test
    public void testFunctionSeesTopLevelVariables() throws TwistException {
        assertEquals("global", exec("g = 'global'; def f() { g }; f()"));
        assertEquals("from java", Twist.exec("def f() { v }; f()", Map.of("v", "from java")));
    }

    @Test
    public void testFunctionLocalsShadowTopLevel() throws TwistException {
        assertEquals(List.of(2, 1), exec("x = 1; def f() { x = 2; x }; [f(), x]"));
    }

    @Test
    public void testFunctionArgumentsShadowTopLevel() throws TwistException {
        assertEquals(List.of("arg", "top"), exec("x = 'top'; def f(x) { x }; [f('arg'), x]"));
    }

    @Test
    public void testFunctionCannotSeeCallerLocals() throws TwistException {
        assertNull(exec("def inner() { secret }; def outer() { secret = 's'; inner() }; outer()"));
    }

    @Test
    public void testFunctionLocalsDoNotLeak() throws TwistException {
        assertNull(exec("def f() { local = 1 }; f(); local"));
    }

    @Test
    public void testJavaFunction() throws TwistException {
        TwistFunction join = (args, ctx) -> String.join("-", args.stream().map(String::valueOf).toList());
        SimpleScriptContext ctx = new SimpleScriptContext(Map.of(), Map.of("join", join));
        assertEquals("a-1-true", Twist.parseScript("join('a', 1, true)").execute(ctx));
    }

    @Test
    public void testAddFunctionAfterContextCreation() throws TwistException {
        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.addFunction("twice", (args, c) -> ValueUtils.asInt(args.get(0)) * 2);
        assertEquals(42, Twist.parseScript("twice(21)").execute(ctx));
        assertTrue(ctx.getFunctionNames().contains("twice"));
    }

    @Test
    public void testBuiltinsTakePrecedence() throws TwistException {
        // A Java function or variable with a built-in's name is never called
        TwistFunction upper = (args, ctx) -> "custom";
        SimpleScriptContext ctx = new SimpleScriptContext(Map.of(), Map.of("upper", upper));
        assertEquals("X", Twist.parseScript("upper('x')").execute(ctx));
        assertEquals(1.0, exec("double = -> (x) { x * 2 }; double(1)"));
    }

    @Test
    public void testDefWithoutParensIsSyntaxError() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("def f { 1 }"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("def f() 1"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("def (a) { a }"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("def f(1) { 1 }"));
    }

    @Test
    public void testLambdaBasics() throws TwistException {
        assertEquals(42, exec("twice = -> (x) { x * 2 }; twice(21)"));
        assertEquals("hi", exec("greet = -> { 'hi' }; greet()"));
        assertEquals("hi", exec("greet = -> () { 'hi' }; greet()"));
    }

    @Test
    public void testLambdaAsArgument() throws TwistException {
        assertEquals(40, exec("apply = -> (f, v) { f(v) }; apply(-> (x) { x * 10 }, 4)"));
        assertEquals(40, exec("def apply(f, v) { f(v) }; apply(-> (x) { x * 10 }, 4)"));
    }

    @Test
    public void testLambdaInCollections() throws TwistException {
        assertEquals(2, exec("fs = [-> (x) { x + 1 }]; fs[0](1)"));
        // A lambda stored in a map can be fetched and called, but o.f(2) would be a Java method call
        assertEquals(6, exec("o = {f: -> (x) { x * 3 }}; g = o.f; g(2)"));
        assertThrows(TwistException.class, () -> exec("o = {f: -> (x) { x * 3 }}; o.f(2)"));
    }

    @Test
    public void testImmediatelyInvokedLambda() throws TwistException {
        assertEquals(3, exec("(-> (a, b) { a + b })(1, 2)"));
        assertEquals(42, Twist.eval("(-> (x) { x * 2 })(21)", Map.of()));
    }

    @Test
    public void testLambdaReturn() throws TwistException {
        assertEquals("posnonpos", exec("f = -> (x) { if (x > 0) return 'pos'; 'nonpos' }; f(1) + f(-1)"));
    }

    @Test
    public void testLambdaIsRecursiveThroughTopLevelVariable() throws TwistException {
        assertEquals(120, exec("fact = -> (n) { n <= 1 ? 1 : n * fact(n - 1) }; fact(5)"));
    }

    @Test
    public void testLambdaPassedFromJava() throws TwistException {
        TwistFunction f = (args, ctx) -> "java:" + args.get(0);
        assertEquals("java:1", Twist.exec("f(1)", Map.of("f", f)));
        assertEquals("java:2", Twist.exec("g = f; g(2)", Map.of("f", f)));
    }

    @Test
    public void testLambdaReturnedToJava() throws TwistException {
        Object result = exec("-> (x) { x * 3 }");
        assertTrue(result instanceof TwistFunction);
        SimpleScriptContext ctx = new SimpleScriptContext();
        assertEquals(12, ((TwistFunction) result).invoke(List.of(4), ctx));
    }

    @Test
    public void testCallingNonFunction() {
        assertThrows(TwistException.class, () -> exec("x = 5; x(1)"));
        assertThrows(TwistException.class, () -> Twist.eval("'justAString'(200)", Map.of()));
        assertThrows(TwistException.class, () -> exec("missing(1)"));
    }

    @Test
    public void testLambdaSyntaxErrors() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("f = -> (x) x * 2"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("f = -> (x { x }"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("f = -> ('a') { 1 }"));
    }

    @Test
    public void testCurriedCallChain() throws TwistException {
        // add(1) returns a function, which is then called with (2)
        assertEquals(2, exec("add = -> (a) { -> (b) { b } }; add(1)(2)"));
    }

    @Test
    public void testClosureCapture() throws TwistException {
        assertEquals(3, exec("add = -> (a) { -> (b) { a + b } }; add(1)(2)"));
        assertEquals(6, exec("def mk(n) { return -> (x) { x + n } }; add5 = mk(5); add5(1)"));
        assertEquals("hi there", exec("greeting = 'hi'; f = -> (who) { greeting + ' ' + who }; f('there')"));
    }

    @Test
    public void testClosureSeesLaterValueOfCapturedVariable() throws TwistException {
        // The scope is shared, not copied, so the lambda sees the current value
        assertEquals(2, exec("n = 1; f = -> { n }; n = 2; f()"));
    }

    @Test
    public void testClosureWritesReachCapturedVariable() throws TwistException {
        assertEquals(3, exec("count = 0; bump = -> { count += 1 }; bump(); bump(); bump(); count"));
        assertEquals("ab", exec("out = ''; add = -> (c) { out += c }; add('a'); add('b'); out"));
    }

    @Test
    public void testClosureArgumentsAndLocalsStayLocal() throws TwistException {
        // A parameter never overwrites a captured variable of the same name
        assertEquals(List.of(10, 1), exec("x = 1; f = -> (x) { x * 10 }; [f(1), x]"));
        // ... but a plain assignment to a name that isn't captured creates a local
        assertNull(exec("f = -> { tmp = 5 }; f(); tmp"));
    }

    @Test
    public void testEachCallCapturesItsOwnScope() throws TwistException {
        assertEquals(List.of(1, 2), exec("""
                def mk(n) { return -> { n } }
                a = mk(1)
                b = mk(2)
                return [a(), b()]
                """));
    }

    @Test
    public void testLambdasInALoopShareTheLoopScope() throws TwistException {
        // A for-each loop has one scope for the whole loop, not one per pass, so lambdas made
        // inside it all see the final value of the loop variable.
        assertEquals(List.of(3, 3, 3), exec("""
                fs = []
                for (i : [1, 2, 3]) {
                    fs.add(-> { i })
                }
                return [fs[0](), fs[1](), fs[2]()]
                """));
    }

    @Test
    public void testDefBodyKeepsItsOwnLocals() throws TwistException {
        // def is not a closure: assigning in its body makes a local, as before
        assertEquals(List.of(2, 1), exec("x = 1; def f() { x = 2; x }; [f(), x]"));
    }

    @Test
    public void testCallInsideLargerExpression() throws TwistException {
        assertEquals(2, exec("f = -> (x) { x }; (f)(1) + 1"));
        assertEquals(3, exec("fs = [-> (x) { x }]; fs[0](1) + 2"));
        assertEquals(12, exec("f = -> (x) { x }; f(2) * f(6)"));
        assertEquals("yes", exec("f = -> (x) { x }; f(1) == 1 ? 'yes' : 'no'"));
        assertEquals(6, exec("m = {f: -> (x) { x * 2 }}; m['f'](3)"));
        // A call chain: add(1) returns a function that is then called
        assertEquals(2, exec("add = -> (a) { -> (b) { b } }; add(1)(2) "));
    }

    @Test
    public void testLambdaCarriesItsScopeToAnotherContext() throws TwistException {
        // The lambda captures v where it was defined, and keeps it when called from elsewhere
        SimpleScriptContext defining = new SimpleScriptContext(Map.of("v", 10), Map.of());
        Object f = Twist.parseScript("-> (x) { x + v }").execute(defining);

        MapContext caller = new MapContext(Map.of("v", 999, "f", f));
        assertEquals(11, Twist.parseExpression("f(1)").evaluate(caller));
    }
}
