package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.expression.function.AssertThrowsFunction;
import com.inksetter.twist.expression.function.FunctionArgumentException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for assertThrows(), the example of a function that reads its arguments as expressions.
 */
public class AssertThrowsTest {

    private Object exec(String script) throws TwistException {
        return exec(script, Map.of());
    }

    private Object exec(String script, Map<String, Object> vars) throws TwistException {
        ScriptContext ctx = new SimpleScriptContext(vars, Map.of());
        ctx.addFunction("assertThrows", new AssertThrowsFunction());
        return Twist.parseScript(script).execute(ctx);
    }

    @Test
    public void testExactType() throws TwistException {
        Object result = exec("assertThrows(NumberFormatException, -> { int('x') })");
        assertTrue(result instanceof NumberFormatException);
    }

    @Test
    public void testSuperclass() throws TwistException {
        assertTrue(exec("assertThrows(Exception, -> { int('x') })") instanceof NumberFormatException);
        assertTrue(exec("assertThrows(RuntimeException, -> { int('x') })") instanceof NumberFormatException);
    }

    @Test
    public void testExceptionFromJavaMethod() throws TwistException {
        // The exception a Java method threw is wrapped in a TwistException; matching sees through it
        assertTrue(exec("assertThrows(StringIndexOutOfBoundsException, -> { 'abc'.substring(10) })")
                instanceof StringIndexOutOfBoundsException);
        assertTrue(exec("assertThrows(IndexOutOfBoundsException, -> { 'abc'.substring(10) })")
                instanceof StringIndexOutOfBoundsException);
    }

    @Test
    public void testWrapperByName() throws TwistException {
        assertTrue(exec("assertThrows(TwistException, -> { 'abc'.substring(10) })") instanceof TwistException);
    }

    @Test
    public void testBareExpressionBlock() throws TwistException {
        assertTrue(exec("assertThrows(NumberFormatException, int('x'))") instanceof NumberFormatException);
        assertTrue(exec("assertThrows(DivideByZeroException, 1 / 0)") instanceof Exception);
        assertTrue(exec("x = 'abc'; assertThrows(StringIndexOutOfBoundsException, x.substring(10))")
                instanceof StringIndexOutOfBoundsException);
    }

    @Test
    public void testReturnedExceptionIsUsable() throws TwistException {
        assertEquals("For input string: \"x\"",
                exec("e = assertThrows(NumberFormatException, -> { int('x') }); e.getMessage()"));
        assertEquals(true,
                exec("assertThrows(NumberFormatException, -> { int('x') }).getMessage() =~ 'input string'"));
    }

    @Test
    public void testNothingThrown() {
        TwistException e = assertThrows(TwistException.class,
                () -> exec("assertThrows(NumberFormatException, -> { int('42') })"));
        assertTrue(e.getMessage(), e.getMessage().contains("NumberFormatException"));
        assertTrue(e.getMessage(), e.getMessage().contains("nothing was thrown"));
    }

    @Test
    public void testWrongType() {
        TwistException e = assertThrows(TwistException.class,
                () -> exec("assertThrows(NumberFormatException, -> { 1 / 0 })"));
        assertTrue(e.getMessage(), e.getMessage().contains("NumberFormatException"));
        // The exception that was actually thrown is kept as the cause
        assertNotNull(e.getCause());
        assertEquals("DivideByZeroException", e.getCause().getClass().getSimpleName());
    }

    @Test
    public void testTypeAsString() throws TwistException {
        assertTrue(exec("assertThrows('NumberFormatException', -> { int('x') })") instanceof NumberFormatException);
    }

    @Test
    public void testTypeFromVariable() throws TwistException {
        // A defined variable wins over the bare name, and can hold a Class or a String
        assertTrue(exec("assertThrows(cls, -> { int('x') })", Map.of("cls", NumberFormatException.class))
                instanceof NumberFormatException);
        assertTrue(exec("assertThrows(cls, -> { int('x') })", Map.of("cls", "NumberFormatException"))
                instanceof NumberFormatException);
    }

    @Test
    public void testBadArguments() {
        assertThrows(FunctionArgumentException.class, () -> exec("assertThrows(NumberFormatException)"));
        assertThrows(FunctionArgumentException.class, () -> exec("assertThrows(NumberFormatException, -> { 1 }, 3)"));
        assertThrows(FunctionArgumentException.class, () -> exec("assertThrows(42, -> { int('x') })"));
        assertThrows(FunctionArgumentException.class,
                () -> exec("assertThrows(cls, -> { int('x') })", Map.of("cls", 42)));
    }

    @Test
    public void testBlockRunsExactlyOnce() throws TwistException {
        assertEquals(1, exec("""
                calls = 0
                assertThrows(NumberFormatException, -> { calls += 1; int('x') })
                calls
                """));
        assertEquals(1, exec("""
                calls = 0
                assertThrows(NumberFormatException, int(string(calls += 1) + 'x'))
                calls
                """));
    }

    @Test
    public void testNestedInsideTry() throws TwistException {
        // A failed assertion is an ordinary exception, so a script can catch it
        assertEquals("failed", exec("""
                try {
                    assertThrows(NumberFormatException, -> { 42 })
                    'passed'
                }
                catch (TwistException e) {
                    'failed'
                }
                """));
    }

    @Test
    public void testCalledAsAValue() throws TwistException {
        // A raw-arg function reached through a variable still sees its arguments as expressions
        ScriptContext ctx = new SimpleScriptContext(Map.of("check", new AssertThrowsFunction()), Map.of());
        Object result = Twist.parseScript("check(NumberFormatException, -> { int('x') })").execute(ctx);
        assertTrue(result instanceof NumberFormatException);
    }
}
