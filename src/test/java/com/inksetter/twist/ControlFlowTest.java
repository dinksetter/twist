package com.inksetter.twist;

import com.inksetter.twist.expression.TypeMismatchException;
import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.expression.operators.arith.DivideByZeroException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ControlFlowTest {
    private final List<Object> logged = new ArrayList<>();
    private final Map<String, TwistFunction> functions = Map.of("log", (args, ctx) -> {
        logged.add(args.get(0));
        return null;
    });

    private Object exec(String script) throws TwistException {
        return exec(script, Map.of());
    }

    private Object exec(String script, Map<String, Object> vars) throws TwistException {
        return Twist.parseScript(script).execute(new SimpleScriptContext(vars, functions));
    }

    @Test
    public void testScriptResultIsLastStatement() throws TwistException {
        assertEquals(3, exec("1; 2; 3"));
        assertEquals(208, exec("a = 100\nb = a + 4; c = b * 2"));
    }

    @Test
    public void testIfElseChain() throws TwistException {
        String script = """
                if (score >= 90) grade = 'A'
                else if (score >= 80) grade = 'B'
                else grade = 'C'
                """;
        assertEquals("A", exec(script, Map.of("score", 95)));
        assertEquals("B", exec(script, Map.of("score", 85)));
        assertEquals("C", exec(script, Map.of("score", 10)));
    }

    @Test
    public void testIfWithBlocks() throws TwistException {
        String script = """
                result = null
                if (x > 3) {
                    result = 'yes'
                } else {
                    result = 'no'
                }
                result
                """;
        assertEquals("yes", exec(script, Map.of("x", 5)));
        assertEquals("no", exec(script, Map.of("x", 1)));
    }

    @Test
    public void testIfWithoutElseHasNullValue() throws TwistException {
        assertNull(exec("if (false) 'x'"));
        assertEquals("x", exec("if (true) 'x'"));
    }

    @Test
    public void testIfTruthiness() throws TwistException {
        assertEquals("no", exec("if ('') 'yes' else 'no'"));
        assertEquals("no", exec("if (0) 'yes' else 'no'"));
        assertEquals("no", exec("if ([]) 'yes' else 'no'"));
        assertEquals("no", exec("if ({}) 'yes' else 'no'"));
        assertEquals("no", exec("if (missing) 'yes' else 'no'"));
        assertEquals("yes", exec("if ([0]) 'yes' else 'no'"));
        assertEquals("yes", exec("if ('false') 'yes' else 'no'"));
    }

    @Test
    public void testCStyleFor() throws TwistException {
        assertEquals(55, exec("t = 0; for (i = 1; i <= 10; i++) { t += i }; t"));
        // The loop variable stays set after the loop
        assertEquals(11, exec("for (i = 1; i <= 10; i++) {}; i"));
        // A loop whose condition is false from the start never runs
        assertEquals(0, exec("t = 0; for (i = 0; i < 0; i++) { t += 1 }; t"));
    }

    @Test
    public void testForWithoutBraces() throws TwistException {
        assertEquals(10, exec("t = 0; for (x : [1, 2, 3, 4]) t += x; t"));
    }

    @Test
    public void testForEach() throws TwistException {
        assertEquals("abc", exec("out = ''; for (x : ['a', 'b', 'c']) { out += x }; out"));
        assertEquals("", exec("out = ''; for (x : []) { out += x }; out"));
        assertEquals("onetwo", exec("out = ''; for (x : arr) { out += x }; out",
                Map.of("arr", List.of("one", "two"))));
    }

    @Test
    public void testForEachOverJavaIterable() throws TwistException {
        assertEquals(6, exec("t = 0; for (x : s) { t += x }; t", Map.of("s", new java.util.TreeSet<>(List.of(1, 2, 3)))));
    }

    @Test
    public void testForEachVariableIsLoopScoped() throws TwistException {
        assertNull(exec("for (x : [1, 2]) {}; x"));
    }

    @Test
    public void testForEachOverNonIterable() {
        assertThrows(TypeMismatchException.class, () -> exec("for (x : 5) {}"));
        assertThrows(TypeMismatchException.class, () -> exec("for (k : {a: 1}) {}"));
    }

    @Test
    public void testNestedLoops() throws TwistException {
        exec("""
                for (i : [1, 2]) {
                    for (j : ['a', 'b']) {
                        log(string(i) + j)
                    }
                }
                """);
        assertEquals(List.of("1a", "1b", "2a", "2b"), logged);
    }

    @Test
    public void testReturnFromLoop() throws TwistException {
        assertEquals(20, exec("def f() { for (x : [1, 2, 3]) { if (x == 2) return x * 10 } }; f()"));
        assertEquals(30, exec("for (i = 1; i < 10; i++) { if (i == 3) return i * 10 }; 'not reached'"));
        assertEquals("early", exec("for (x : [1]) { return 'early' }; 'late'"));
    }

    @Test
    public void testReturnFromNestedBlock() throws TwistException {
        assertEquals("inner", exec("{ { return 'inner' }; 'middle' }; 'outer'"));
    }

    @Test
    public void testBlockScope() throws TwistException {
        // New variables are local to the block
        assertNull(exec("{ inner = 1 }; inner"));
        assertNull(exec("if (true) { z = 1 }; z"));
        // Existing variables are updated
        assertEquals(1, exec("z = 0; if (true) { z = 1 }; z"));
        assertEquals(Arrays.asList(true, null, 400), exec("""
                found = false
                last = null
                for (x : [5, 200]) {
                    if (x > 100) { found = true }
                    tmp = x * 2
                    last = tmp
                }
                return [found, tmp, last]
                """));
    }

    @Test
    public void testTryCatchFinally() throws TwistException {
        assertEquals("abc", exec("""
                log = ''
                try {
                    log += 'a'
                    int('x')
                    log += 'not reached'
                }
                catch (NumberFormatException e) { log += 'b' }
                finally { log += 'c' }
                log
                """));
    }

    @Test
    public void testFinallyRunsWithoutException() throws TwistException {
        assertEquals("ac", exec("log = ''; try { log += 'a' } catch (Exception e) { log += 'b' } finally { log += 'c' }; log"));
    }

    @Test
    public void testFinallyWithoutCatch() throws TwistException {
        assertEquals("ab", exec("log = ''; try { log += 'a' } finally { log += 'b' }; log"));
    }

    @Test
    public void testFinallyRunsWhenExceptionPropagates() {
        assertThrows(NumberFormatException.class, () -> exec("try { int('x') } finally { log('finally') }"));
        assertEquals(List.of("finally"), logged);
    }

    @Test
    public void testFinallyRunsOnReturn() throws TwistException {
        assertEquals("from try", exec("try { return 'from try' } finally { log('finally') }; 'after'"));
        assertEquals(List.of("finally"), logged);
    }

    @Test
    public void testTryValue() throws TwistException {
        assertEquals("ok", exec("try { 'ok' } catch (Exception e) { 'caught' }"));
        assertEquals("caught", exec("try { int('x') } catch (Exception e) { 'caught' }"));
    }

    @Test
    public void testCatchVariable() throws TwistException {
        assertEquals("For input string: \"x\"", exec("try { int('x') } catch (Exception e) { e.getMessage() }"));
        assertEquals("NumberFormatException", exec("try { int('x') } catch (Exception e) { e.getClass().getSimpleName() }"));
    }

    @Test
    public void testCatchVariableIsScopedToCatchBlock() throws TwistException {
        assertNull(exec("try { int('x') } catch (Exception e) { 1 }; e"));
    }

    @Test
    public void testMultipleCatchBlocks() throws TwistException {
        String script = """
                try { risky() }
                catch (NumberFormatException e) { 'nfe' }
                catch (DivideByZeroException e) { 'div' }
                catch (Exception e) { 'other' }
                """;
        assertEquals("nfe", exec(script.replace("risky()", "int('x')")));
        assertEquals("div", exec(script.replace("risky()", "1 / 0")));
        assertEquals("other", exec(script.replace("risky()", "for (x : 5) {}")));
    }

    @Test
    public void testFirstMatchingCatchWins() throws TwistException {
        assertEquals("general", exec("try { int('x') } catch (Exception e) { 'general' } catch (NumberFormatException e) { 'specific' }"));
    }

    @Test
    public void testUnmatchedCatchRethrows() {
        assertThrows(DivideByZeroException.class, () -> exec("try { 1 / 0 } catch (NumberFormatException e) { 'nfe' }"));
    }

    @Test
    public void testEmptyCatchSwallowsException() throws TwistException {
        assertEquals("after", exec("try { int('x') } catch (Exception e) {}; 'after'"));
        assertNull(exec("try { int('x') } catch (NumberFormatException e) {}"));
        // An empty catch that doesn't match still rethrows
        assertThrows(DivideByZeroException.class, () -> exec("try { 1 / 0 } catch (NumberFormatException e) {}"));
    }

    @Test
    public void testNestedTry() throws TwistException {
        assertEquals("inner outer", exec("""
                log = ''
                try {
                    try { 1 / 0 }
                    catch (DivideByZeroException e) { log += 'inner'; int('x') }
                }
                catch (NumberFormatException e) { log += ' outer' }
                log
                """));
    }

    @Test
    public void testCatchTwistExceptionCause() throws TwistException {
        // Exceptions thrown by Java code are wrapped in a TwistException; catch matches the cause.
        TwistFunction thrower = (args, ctx) -> {
            throw new TwistException("wrapped", new IllegalStateException("boom"));
        };
        String script = "try { thrower() } catch (IllegalStateException e) { 'caught' }";
        Object result = Twist.parseScript(script).execute(new SimpleScriptContext(Map.of(), Map.of("thrower", thrower)));
        assertEquals("caught", result);
    }

    @Test
    public void testCatchExceptionFromJavaMethod() throws TwistException {
        assertEquals("caught", exec("try { 'abc'.substring(10) } catch (StringIndexOutOfBoundsException e) { 'caught' }"));
        // Superclasses of the cause match too
        assertEquals("caught", exec("try { 'abc'.substring(10) } catch (IndexOutOfBoundsException e) { 'caught' }"));
        assertEquals("caught", exec("try { 'abc'.substring(10) } catch (Exception e) { 'caught' }"));
        // So does the TwistException wrapper itself
        assertEquals("caught", exec("try { 'abc'.substring(10) } catch (TwistException e) { 'caught' }"));
        // The catch variable holds the exception the method threw
        assertEquals("StringIndexOutOfBoundsException",
                exec("try { 'abc'.substring(10) } catch (Exception e) { e.getClass().getSimpleName() }"));
        // An unrelated type still propagates
        assertThrows(TwistException.class,
                () -> exec("try { 'abc'.substring(10) } catch (NumberFormatException e) { 'caught' }"));
    }

    @Test
    public void testScriptCanRunRepeatedlyWithDifferentContexts() throws TwistException {
        Script script = Twist.parseScript("t = 0; for (x : items) { t += x }; t");
        assertEquals(6, script.execute(new SimpleScriptContext(Map.of("items", List.of(1, 2, 3)), Map.of())));
        assertEquals(0, script.execute(new SimpleScriptContext(Map.of("items", List.of()), Map.of())));
        Map<String, Object> vars = new HashMap<>();
        vars.put("items", List.of(10));
        assertEquals(10, script.execute(new SimpleScriptContext(vars, Map.of())));
    }
}
