package com.inksetter.twist;

import com.inksetter.twist.expression.function.FunctionArgumentException;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BuiltinFunctionTest {
    private static final Map<String, Object> VARS = Map.of("name", "World", "n", 7);

    private static Object eval(String expr) throws TwistException {
        return Twist.eval(expr, VARS);
    }

    private static Object eval(String expr, Map<String, Object> vars) throws TwistException {
        return Twist.eval(expr, vars);
    }

    @Test
    public void testFunctionNamesAreCaseInsensitive() throws TwistException {
        assertEquals("WORLD", eval("UPPER(name)"));
        assertEquals("WORLD", eval("Upper(name)"));
    }

    @Test
    public void testUnknownFunction() {
        assertThrows(TwistException.class, () -> eval("noSuchFunction(1)"));
    }

    @Test
    public void testStringConversion() throws TwistException {
        assertEquals("7", eval("string(n)"));
        assertEquals("3", eval("string(3.0)"));
        assertEquals("3.25", eval("string(3.25)"));
        assertEquals("true", eval("string(true)"));
        assertNull(eval("string(null)"));
    }

    @Test
    public void testIntConversion() throws TwistException {
        assertEquals(43, eval("int('42') + 1"));
        assertEquals(3, eval("int(3.9)"));
        assertEquals(1, eval("int(true)"));
        assertNull(eval("int('')"));
        assertNull(eval("int(null)"));
        assertThrows(NumberFormatException.class, () -> eval("int('abc')"));
    }

    @Test
    public void testDoubleConversion() throws TwistException {
        assertEquals(3.0, eval("double('1.5') * 2"));
        assertEquals(7.0, eval("double(n)"));
        assertEquals(0.0, eval("double(null)"));
        assertThrows(NumberFormatException.class, () -> eval("double('abc')"));
    }

    @Test
    public void testCaseAndTrim() throws TwistException {
        assertEquals("WORLD", eval("upper(name)"));
        assertEquals("world", eval("lower(name)"));
        assertEquals("x y", eval("trim('  x y  ')"));
        assertNull(eval("upper(null)"));
        assertNull(eval("lower(null)"));
        assertNull(eval("trim(null)"));
    }

    @Test
    public void testLength() throws TwistException {
        assertEquals(5, eval("len(name)"));
        assertEquals(5, eval("length(name)"));
        assertEquals(0, eval("len('')"));
        assertEquals(0, eval("len(null)"));
        assertEquals(3, eval("len(123)"));
    }

    @Test
    public void testSubstr() throws TwistException {
        assertEquals("orl", eval("substr('World', 2, 3)"));
        assertEquals("orld", eval("substr('World', 2)"));
        assertEquals("World", eval("substr('World', 1)"));
        // 0 is treated like 1
        assertEquals("World", eval("substr('World', 0)"));
        // Negative starts count from the end
        assertEquals("rld", eval("substr('World', -3)"));
        assertEquals("rl", eval("substr('World', -3, 2)"));
        // Out-of-range values are clamped
        assertEquals("", eval("substr('World', 10)"));
        assertEquals("rld", eval("substr('World', 3, 100)"));
        assertEquals("", eval("substr('World', 2, 0)"));
        assertNull(eval("substr(null, 1)"));
    }

    @Test
    public void testSubstrArgumentCount() {
        assertThrows(FunctionArgumentException.class, () -> eval("substr('World')"));
        assertThrows(FunctionArgumentException.class, () -> eval("substr('World', 1, 2, 3)"));
    }

    @Test
    public void testIndexOf() throws TwistException {
        assertEquals(3, eval("indexof(name, 'r')"));
        assertEquals(1, eval("indexof(name, 'W')"));
        assertEquals(0, eval("indexof(name, 'z')"));
        assertEquals(4, eval("indexof('abcabc', 'a', 1)"));
        assertThrows(FunctionArgumentException.class, () -> eval("indexof(name)"));
    }

    @Test
    public void testSprintf() throws TwistException {
        assertEquals("World has 3 items", eval("sprintf('%s has %d items', name, 3)"));
        assertEquals("007", eval("sprintf('%03d', n)"));
        assertEquals("plain", eval("sprintf('plain')"));
        assertThrows(FunctionArgumentException.class, () -> eval("sprintf()"));
    }

    @Test
    public void testMinMax() throws TwistException {
        assertEquals(2, eval("min(4, 2, 9)"));
        assertEquals(9, eval("max(4, 2, 9)"));
        assertEquals(1.5, eval("min(4, 1.5, 9)"));
        assertEquals("b", eval("max('a', 'b')"));
        assertEquals(5, eval("max(5)"));
        assertNull(eval("max()"));
    }

    @Test
    public void testType() throws TwistException {
        assertEquals("STRING", eval("type(name)"));
        assertEquals("INTEGER", eval("type(n)"));
        assertEquals("DOUBLE", eval("type(1.5)"));
        assertEquals("BOOLEAN", eval("type(true)"));
        assertEquals("ARRAY", eval("type([1])"));
        assertEquals("DATETIME", eval("type(now())"));
    }

    @Test
    public void testDateAndNow() throws TwistException {
        Object parsed = eval("date('2024-05-06T07:03:09Z')");
        assertEquals(Date.from(java.time.Instant.parse("2024-05-06T07:03:09Z")), parsed);
        assertNull(eval("date(null)"));
        assertNull(eval("date('')"));

        long before = System.currentTimeMillis();
        Date now = (Date) eval("now()");
        assertTrue(now.getTime() >= before && now.getTime() <= System.currentTimeMillis());
        assertThrows(FunctionArgumentException.class, () -> eval("now(1)"));
    }

    @Test
    public void testEval() throws TwistException {
        assertEquals(2, eval("eval('{\"a\": [1, 2]}').a[1]"));
        assertEquals(6, eval("eval('2 * 3')"));
        // eval() runs in an empty context, so outer variables are not visible
        assertNull(eval("eval('name')"));
        assertThrows(FunctionArgumentException.class, () -> eval("eval()"));
    }

    @Test
    public void testJson() throws TwistException {
        assertEquals("{\"a\":1,\"b\":[2,3]}", eval("json({a: 1, b: [2, 3]})"));
        assertEquals("[]", eval("json([])"));
        assertEquals("{}", eval("json({})"));
        assertEquals("null", eval("json(null)"));
        assertEquals("1.5", eval("json(1.5)"));
        assertThrows(FunctionArgumentException.class, () -> eval("json()"));
    }

    @Test
    public void testJsonEscapingAndBooleans() throws TwistException {
        assertEquals("[true,false]", eval("json([true, false])"));
        assertEquals("\"say \\\"hi\\\"\"", eval("json('say \"hi\"')"));
        assertEquals("\"a\\nb\"", eval("json('a\nb')"));
        assertEquals("\"a\\\\b\"", eval("json('a\\b')"));
        assertEquals("\"a\\tb\"", eval("json('a\tb')"));
        assertEquals("\"\\u0000\"", eval("json(s)", Map.of("s", "\u0000")));
        // Keys are escaped too
        assertEquals("{\"a\\\"b\":1}", eval("json(m)", Map.of("m", Map.of("a\"b", 1))));
        assertEquals("{\"d\":\"2024-01-01T00:00:00Z\"}", eval("json({d: date('2024-01-01T00:00:00Z')})"));
    }

    @Test
    public void testBase64() throws TwistException {
        byte[] decoded = (byte[]) eval("b64decode('aGVsbG8=')");
        assertEquals("hello", new String(decoded, StandardCharsets.UTF_8));
        assertEquals("aGVsbG8=", eval("b64encode(b64decode('aGVsbG8='))"));
        assertNull(eval("b64encode(null)"));
        assertNull(eval("b64decode(null)"));
        assertThrows(FunctionArgumentException.class, () -> eval("b64encode('not bytes')"));
    }

    @Test
    public void testSingleArgFunctionsRejectWrongArgCount() {
        for (String fn : List.of("upper", "lower", "trim", "len", "string", "int", "double", "type", "date")) {
            assertThrows(fn, FunctionArgumentException.class, () -> eval(fn + "()"));
            assertThrows(fn, FunctionArgumentException.class, () -> eval(fn + "('a', 'b')"));
        }
    }
}
