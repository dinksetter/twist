package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.expression.function.TwistFunction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * A host can extend SimpleScriptContext to give it behavior of its own, most usefully a fallback for
 * names the context doesn't know. Those overrides have to keep working inside blocks, loops,
 * function bodies and lambdas, each of which runs in a nested frame.
 */
public class ScriptContextExtensionTest {

    /**
     * Resolves any function name ending in "_dyn" to a function made up on the spot, and any unknown
     * variable name in capitals to a canned value.
     */
    public static class FallbackContext extends SimpleScriptContext {
        final List<String> functionLookups = new ArrayList<>();
        final List<String> variableLookups = new ArrayList<>();

        FallbackContext(Map<String, Object> initial, Map<String, TwistFunction> functions) {
            super(initial, functions);
        }

        @Override
        public TwistFunction lookupFunction(String name) {
            functionLookups.add(name);
            TwistFunction found = super.lookupFunction(name);
            if (found != null) {
                return found;
            }
            if (name.endsWith("_dyn")) {
                return (args, ctx) -> "dynamic:" + name + args;
            }
            return null;
        }

        @Override
        public Object getVariable(String name) {
            Object value = super.getVariable(name);
            if (value == null && name.equals(name.toUpperCase())) {
                variableLookups.add(name);
                return "canned:" + name;
            }
            return value;
        }
    }

    private FallbackContext ctx() {
        return new FallbackContext(Map.of("known", "from host"), Map.of());
    }

    private Object exec(String script, FallbackContext ctx) throws TwistException {
        return Twist.parseScript(script).execute(ctx);
    }

    @Test
    public void testFallbackFunctionAtTopLevel() throws TwistException {
        assertEquals("dynamic:greet_dyn[1]", exec("greet_dyn(1)", ctx()));
    }

    @Test
    public void testFallbackFunctionInNestedFrames() throws TwistException {
        assertEquals("dynamic:greet_dyn[]", exec("{ greet_dyn() }", ctx()));
        assertEquals("dynamic:greet_dyn[]", exec("if (true) { greet_dyn() }", ctx()));
        assertEquals("dynamic:greet_dyn[1]", exec("out = null; for (x : [1]) { out = greet_dyn(x) }; out", ctx()));
        assertEquals("dynamic:greet_dyn[]", exec("def f() { greet_dyn() }; f()", ctx()));
        assertEquals("dynamic:greet_dyn[]", exec("f = -> { greet_dyn() }; f()", ctx()));
        assertEquals("dynamic:greet_dyn[]",
                exec("try { int('x') } catch (Exception e) { greet_dyn() }", ctx()));
        assertEquals("dynamic:greet_dyn[2]",
                exec("def outer() { for (x : [2]) { return greet_dyn(x) } }; outer()", ctx()));
    }

    @Test
    public void testRegisteredFunctionsStillWin() throws TwistException {
        FallbackContext ctx = new FallbackContext(Map.of(),
                Map.of("greet_dyn", (args, c) -> "registered"));
        assertEquals("registered", exec("def f() { greet_dyn() }; f()", ctx));
    }

    @Test
    public void testOverrideSeesTheSameInstanceFromEveryFrame() throws TwistException {
        FallbackContext ctx = ctx();
        exec("def f() { greet_dyn() }; f(); { other_dyn() }", ctx);
        // Both lookups were recorded on the instance the host created
        assertTrue(ctx.functionLookups.toString(), ctx.functionLookups.contains("greet_dyn"));
        assertTrue(ctx.functionLookups.toString(), ctx.functionLookups.contains("other_dyn"));
    }

    @Test
    public void testFunctionDefinedInNestedFrameIsVisibleToTheOverride() throws TwistException {
        FallbackContext ctx = ctx();
        assertEquals("inner", exec("{ def inner() { 'inner' } }; inner()", ctx));
        assertNotNull(ctx.lookupFunction("inner"));
    }

    @Test
    public void testFallbackVariableInNestedFrames() throws TwistException {
        assertEquals("canned:SOMETHING", exec("SOMETHING", ctx()));
        assertEquals("canned:SOMETHING", exec("{ SOMETHING }", ctx()));
        assertEquals("canned:SOMETHING", exec("def f() { SOMETHING }; f()", ctx()));
        assertEquals("canned:SOMETHING", exec("f = -> { SOMETHING }; f()", ctx()));
        assertEquals("canned:SOMETHING", exec("out = null; for (x : [1]) { out = SOMETHING }; out", ctx()));
    }

    @Test
    public void testHostVariablesAndOverrideCoexist() throws TwistException {
        FallbackContext ctx = ctx();
        assertEquals(List.of("from host", "canned:OTHER"), exec("def f() { return [known, OTHER] }; f()", ctx));
        assertEquals(List.of("OTHER"), ctx.variableLookups);
    }

    @Test
    public void testScriptVariablesStillShadowTheFallback() throws TwistException {
        assertEquals("set by script", exec("SOMETHING = 'set by script'; SOMETHING", ctx()));
        assertEquals("set by script", exec("SOMETHING = 'set by script'; def f() { SOMETHING }; f()", ctx()));
    }

    /**
     * Records writes, to show which ones a subclass can see.
     */
    public static class WriteLoggingContext extends SimpleScriptContext {
        final List<String> writes = new ArrayList<>();

        @Override
        public void setVariable(String name, Object value) {
            writes.add(name);
            super.setVariable(name, value);
        }
    }

    @Test
    public void testWritesAtTopLevelAreIntercepted() throws TwistException {
        WriteLoggingContext ctx = new WriteLoggingContext();
        Twist.parseScript("a = 1; b = a + 1").execute(ctx);
        assertEquals(List.of("a", "b"), ctx.writes);
    }

    @Test
    public void testWritesInNestedFramesAreNotIntercepted() throws TwistException {
        // A variable created in a nested scope belongs to that frame, so a subclass doesn't see it.
        // Updating a variable that lives in the root frame is still reported.
        WriteLoggingContext ctx = new WriteLoggingContext();
        Twist.parseScript("""
                outer = 1
                { inner = 2 }
                def f() { local = 3 }
                f()
                """).execute(ctx);
        assertEquals(List.of("outer"), ctx.writes);
        assertEquals(1, ctx.getVariable("outer"));
        assertNull(ctx.getVariable("inner"));
    }

    @Test
    public void testPlainSubclassWithoutOverridesBehavesNormally() throws TwistException {
        class Plain extends SimpleScriptContext {
            Plain() {
                super(Map.of("a", 1), Map.of());
            }
        }
        ScriptContext ctx = new Plain();
        assertEquals(3, Twist.parseScript("b = a + 2; { c = b }; b").execute(ctx));
        assertEquals(3, ctx.getVariable("b"));
    }
}
