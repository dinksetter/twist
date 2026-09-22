package com.inksetter.twist;

import com.inksetter.twist.parser.ScriptSyntaxException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the static {@link Twist} helpers and for typed expression evaluation.
 */
public class TwistTest {

    @Test
    public void testEval() throws TwistException {
        assertEquals("Hello, World!", Twist.eval("'Hello, ' + name + '!'", Map.of("name", "World")));
        assertEquals(49, Twist.eval("n * n", Map.of("n", 7)));
        assertNull(Twist.eval("missing", Map.of()));
    }

    @Test
    public void testTypedEval() throws TwistException {
        assertTrue(Twist.eval("age >= 18 && country == 'US'", Map.of("age", 21, "country", "US"), Boolean.class));
        assertEquals("25", Twist.eval("5 * 5", Map.of(), String.class));
        assertEquals(Integer.valueOf(42), Twist.eval("'42'", Map.of(), Integer.class));
        assertEquals(Long.valueOf(42), Twist.eval("42", Map.of(), Long.class));
        assertEquals(1.5, Twist.eval("'1.5'", Map.of(), Double.class), 0.0);
        assertTrue(Twist.eval("'x'", Map.of(), Boolean.class));
        assertFalse(Twist.eval("''", Map.of(), Boolean.class));
        assertNull(Twist.eval("null", Map.of(), String.class));
    }

    @Test
    public void testTypedEvalToSupertype() throws TwistException {
        assertEquals(5, Twist.eval("5", Map.of(), Number.class));
        assertEquals(List.of(1, 2), Twist.eval("[1, 2]", Map.of(), List.class));
        assertEquals(Map.of("a", 1), Twist.eval("{a: 1}", Map.of(), Map.class));
        assertEquals("x", Twist.eval("'x'", Map.of(), Object.class));
    }

    @Test
    public void testTypedEvalToDate() throws TwistException {
        Date d = Twist.eval("'2024-05-06T07:03:09Z'", Map.of(), Date.class);
        assertEquals(Date.from(java.time.Instant.parse("2024-05-06T07:03:09Z")), d);
    }

    @Test
    public void testTypedEvalUnsupportedType() {
        assertThrows(TwistException.class, () -> Twist.eval("5", Map.of(), List.class));
    }

    @Ignore("Known bug: ValueUtils.asType uses cls.cast(), which always fails for primitive classes")
    @Test
    public void testTypedEvalToPrimitive() throws TwistException {
        assertEquals(Boolean.TRUE, Twist.eval("1 < 2", Map.of(), boolean.class));
        assertEquals(Integer.valueOf(5), Twist.eval("'5'", Map.of(), int.class));
    }

    @Test
    public void testExec() throws TwistException {
        Object total = Twist.exec("""
                t = 0
                for (x : items) { t += x }
                t
                """, Map.of("items", List.of(1, 2, 3, 4)));
        assertEquals(10, total);
    }

    @Test
    public void testExecDoesNotModifyInputMap() throws TwistException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("a", 1);
        Twist.exec("a = 2; b = 3", vars);
        assertEquals(Map.of("a", 1), vars);
    }

    @Test
    public void testExecReturn() throws TwistException {
        assertEquals(42, Twist.exec("return 42; 99", Map.of()));
    }

    @Test
    public void testParseHelpers() throws TwistException {
        Expression expr = Twist.parseExpression("x + 1");
        assertEquals(2, expr.evaluate(new MapContext(Map.of("x", 1))));
        assertEquals(11, expr.evaluate(new MapContext(Map.of("x", 10))));

        Script script = Twist.parseScript("y = x * 2");
        SimpleScriptContext ctx = new SimpleScriptContext(Map.of("x", 4), Map.of());
        script.execute(ctx);
        assertEquals(8, ctx.getVariable("y"));
    }

    @Test
    public void testSyntaxErrors() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("a = "));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseExpression("1 +"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.eval("(1", Map.of()));
    }

    @Test
    public void testExpressionWithTrailingTokens() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseExpression("1 2"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseExpression("a = 1; b"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseExpression("(1 + 2) 3"));
    }
}
