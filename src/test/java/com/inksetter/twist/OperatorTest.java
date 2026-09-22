package com.inksetter.twist;

import com.inksetter.twist.expression.operators.arith.DivideByZeroException;
import com.inksetter.twist.parser.ScriptSyntaxException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OperatorTest {
    private static final Map<String, Object> VARS = Map.of(
            "name", "World",
            "n", 7,
            "empty", "",
            "items", List.of(3, 1, 2));

    private static Object eval(String expr) throws TwistException {
        return Twist.eval(expr, VARS);
    }

    private static boolean test(String expr) throws TwistException {
        return Twist.eval(expr, VARS, Boolean.class);
    }

    private static Object exec(String script) throws TwistException {
        return Twist.exec(script, new HashMap<>());
    }

    @Test
    public void testIntegerArithmetic() throws TwistException {
        assertEquals(12, eval("n + 5"));
        assertEquals(2, eval("n - 5"));
        assertEquals(35, eval("n * 5"));
        assertEquals(3, eval("7 / 2"));
        assertEquals(1, eval("n % 3"));
        assertEquals(-3, eval("2 - 5"));
        assertEquals(14, eval("2 + 3 * 4"));
        assertEquals(20, eval("(2 + 3) * 4"));
        assertEquals(1, eval("10 - 4 - 5"));
        assertEquals(1, eval("20 / 4 / 5"));
    }

    @Test
    public void testDoubleArithmetic() throws TwistException {
        assertEquals(3.5, eval("7 / 2.0"));
        assertEquals(3.5, eval("7.0 / 2"));
        assertEquals(4.5, eval("1.5 * 3"));
        assertEquals(0.5, eval("1 - 0.5"));
        assertEquals(1000.0, eval("1e3"));
    }

    @Test
    public void testNumericLiterals() throws TwistException {
        assertEquals(-3, eval("-3"));
        assertEquals(-2.5, eval("-2.5"));
        assertEquals(3, eval("+3"));
        assertEquals(Integer.MAX_VALUE, eval(String.valueOf(Integer.MAX_VALUE)));
        // Values beyond the int range are promoted to double
        assertEquals(3.0e9, eval("3000000000"));
    }

    @Ignore("Known gap: unary minus is only supported in front of numeric literals")
    @Test
    public void testUnaryMinusOnExpression() throws TwistException {
        assertEquals(-7, eval("-n"));
        assertEquals(-7, eval("-(n)"));
    }

    @Test
    public void testStringArithmetic() throws TwistException {
        assertEquals("Hello, World", eval("'Hello, ' + name"));
        assertEquals("a1", eval("'a' + 1"));
        assertEquals("a1.5", eval("'a' + 1.5"));
        assertEquals("atrue", eval("'a' + true"));
        assertEquals(6, eval("'3' * 2"));
        // + concatenates when the left side is a string, even if it looks like a number
        assertEquals("32", eval("'3' + 2"));
    }

    @Test
    public void testNumberPlusNonNumericStringFails() {
        assertThrows(NumberFormatException.class, () -> eval("1 + 'a'"));
    }

    @Test
    public void testNullArithmetic() throws TwistException {
        assertEquals(1, eval("null + 1"));
        assertEquals(1, eval("missing + 1"));
    }

    @Test
    public void testDivideByZero() {
        assertThrows(DivideByZeroException.class, () -> eval("1 / 0"));
        assertThrows(DivideByZeroException.class, () -> eval("1.5 / 0"));
        assertThrows(DivideByZeroException.class, () -> eval("1 % 0"));
    }

    @Ignore("Known bug: ModExpression converts both sides to int")
    @Test
    public void testDoubleModulo() throws TwistException {
        assertEquals(1.5, eval("7.5 % 2"));
    }

    @Test
    public void testComparison() throws TwistException {
        assertTrue(test("n == 7"));
        assertTrue(test("n != 8"));
        assertTrue(test("n <> 8"));
        assertFalse(test("n <> 7"));
        assertTrue(test("n < 8"));
        assertTrue(test("n <= 7"));
        assertTrue(test("n > 6"));
        assertTrue(test("n >= 7"));
        assertFalse(test("n > 7"));
        assertFalse(test("n < 7"));
        assertTrue(test("1.5 < 2"));
        assertTrue(test("'abc' < 'abd'"));
    }

    @Test
    public void testMixedTypeComparison() throws TwistException {
        assertTrue(test("'1' == 1"));
        assertTrue(test("1 == 1.0"));
        // A string on either side makes it a string comparison
        assertTrue(test("'10' < 9"));
        assertTrue(test("missing == null"));
        // Unlike ?:, == does not treat an empty string as null
        assertFalse(test("empty == null"));
    }

    @Test
    public void testLogicalOperators() throws TwistException {
        assertTrue(test("n > 1 && n < 10"));
        assertFalse(test("n > 1 && n > 10"));
        assertTrue(test("n > 100 || n == 7"));
        assertFalse(test("n > 100 || n < 0"));
        assertFalse(test("!(n > 1)"));
        assertTrue(test("!false"));
        assertTrue(test("true && 'x'"));
        assertFalse(test("true && ''"));
        // && binds tighter than ||
        assertTrue(test("true || false && false"));
    }

    @Test
    public void testLogicalShortCircuit() throws TwistException {
        // The right side would throw if evaluated
        assertFalse(test("false && 1 / 0 == 1"));
        assertTrue(test("true || 1 / 0 == 1"));
    }

    @Test
    public void testLike() throws TwistException {
        assertTrue(test("name like 'World'"));
        assertTrue(test("name like 'W%'"));
        assertTrue(test("name like '%d'"));
        assertTrue(test("name like '%or%'"));
        assertTrue(test("name like 'W%d'"));
        assertTrue(test("name like 'W_rld'"));
        assertTrue(test("name like 'World%'"));
        assertFalse(test("name like 'W_d'"));
        assertFalse(test("name like 'world'"));
        assertFalse(test("name like 'Worl'"));
        assertTrue(test("name not like 'X%'"));
        assertFalse(test("name not like 'W%'"));
    }

    @Test
    public void testNotRequiresLike() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseExpression("name not 'x'"));
    }

    @Test
    public void testRegexOperators() throws TwistException {
        assertTrue(test("name =~ 'orl'"));
        assertFalse(test("name =~ 'xyz'"));
        assertTrue(test("name ==~ 'W.*d'"));
        assertFalse(test("name ==~ 'orl'"));
    }

    @Test
    public void testRegexNoMatch() throws TwistException {
        assertTrue(test("name !~ '[0-9]+'"));
        assertFalse(test("name !~ 'W.*'"));
        // Like ==~, the whole string must match, so a partial match still counts as no match
        assertTrue(test("name !~ 'orl'"));
        // Regex syntax, not like-pattern syntax
        assertTrue(test("name !~ 'W%'"));
        assertFalse(test("name !~ 'W...d'"));
    }

    @Test
    public void testTernary() throws TwistException {
        assertEquals("big", eval("n > 5 ? 'big' : 'small'"));
        assertEquals("small", eval("n > 50 ? 'big' : 'small'"));
        assertEquals("seven", eval("n == 1 ? 'one' : n == 7 ? 'seven' : 'other'"));
        // Only the selected branch is evaluated
        assertEquals(1, eval("true ? 1 : 1 / 0"));
    }

    @Test
    public void testElvis() throws TwistException {
        assertEquals("default", eval("missing ?: 'default'"));
        assertEquals("default", eval("empty ?: 'default'"));
        assertEquals("World", eval("name ?: 'default'"));
        assertEquals(0, eval("0 ?: 5"));
        assertEquals("c", eval("missing ?: empty ?: 'c'"));
    }

    @Test
    public void testAssignmentOperators() throws TwistException {
        assertEquals(2, exec("i = 17; i %= 5; i"));
        assertEquals(20, exec("i = 4; i *= 5; i"));
        assertEquals(2, exec("i = 10; i /= 5; i"));
        assertEquals(0, exec("i = 1; i--; i"));
        assertEquals(3, exec("i = 1; i++; i++; i"));
        assertEquals(List.of(0, 0, 0), exec("a = b = c = 0; [a, b, c]"));
    }

    @Test
    public void testIllegalAssignment() {
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("1 = 2"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("'a' += 2"));
        assertThrows(ScriptSyntaxException.class, () -> Twist.parseScript("f() = 2"));
    }

    @Test
    public void testDateArithmetic() throws TwistException {
        assertEquals(Twist.eval("date('2024-01-02T12:00:00Z')", Map.of()),
                Twist.eval("date('2024-01-01T12:00:00Z') + 1", Map.of()));
        assertEquals(Twist.eval("date('2024-01-01T06:00:00Z')", Map.of()),
                Twist.eval("date('2024-01-01T12:00:00Z') - 0.25", Map.of()));
        assertEquals(Twist.eval("date('2024-01-03T00:00:00Z')", Map.of()),
                Twist.eval("2 + date('2024-01-01T00:00:00Z')", Map.of()));
    }

    @Test
    public void testDateDifference() throws TwistException {
        assertEquals(2.0, eval("date('2024-01-03T00:00:00Z') - date('2024-01-01T00:00:00Z')"));
        assertEquals(-2.0, eval("date('2024-01-01T00:00:00Z') - date('2024-01-03T00:00:00Z')"));
        assertEquals(1.25, eval("date('2024-01-02T06:00:00Z') - date('2024-01-01T00:00:00Z')"));
        assertEquals(-0.5, eval("date('2024-01-01T00:00:00Z') - date('2024-01-01T12:00:00Z')"));
        assertEquals(0.0, eval("date('2024-01-01T00:00:00Z') - date('2024-01-01T00:00:00Z')"));
    }

    @Test
    public void testDateDifferenceRoundTrip() throws TwistException {
        // (d + n) - d == n
        assertEquals(3.5, (Double) exec("d = date('2024-03-01T00:00:00Z'); (d + 3.5) - d"), 1e-9);
        assertEquals(-3.5, (Double) exec("d = date('2024-03-01T00:00:00Z'); (d - 3.5) - d"), 1e-9);
    }

    @Ignore("Known bug: ValueUtils.formatDate formats an Instant with ISO_DATE_TIME, which throws")
    @Test
    public void testDateComparison() throws TwistException {
        assertTrue(test("date('2024-01-01T00:00:00Z') < date('2024-01-02T00:00:00Z')"));
        assertTrue(test("now() + 1 > now()"));
    }

    @Ignore("Known bug: a date on the right of + is treated as date arithmetic even when the left side is a string")
    @Test
    public void testStringPlusDate() throws TwistException {
        assertTrue(eval("'at ' + now()") instanceof String);
    }
}
