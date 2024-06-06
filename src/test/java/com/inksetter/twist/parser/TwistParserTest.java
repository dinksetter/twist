package com.inksetter.twist.parser;

import com.inksetter.twist.exec.SimpleContext;
import com.inksetter.twist.expression.Expression;
import org.junit.Test;

import static org.junit.Assert.*;

public class TwistParserTest {
    

    @Test
    public void testEmptyExpression() {
        try {
            Expression parsed = new TwistParser("").parseExpression();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (TwistParseException e) {
            // Normal
        }
    }

    @Test
    public void testIntConstant() throws TwistParseException {
        Expression parsed = new TwistParser("100").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals(100, result);
        parsed = new TwistParser("-4").parseExpression();
        result = parsed.evaluate(new SimpleContext());
        assertEquals(-4, result);
    }

    @Test
    public void testDoubleConstant() throws TwistParseException {
        Expression parsed = new TwistParser("10.2").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals(10.2, result);
    }

    @Test
    public void testStringConstant() throws TwistParseException {
        Expression parsed = new TwistParser("'foo'").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals("foo", result);
    }

    @Test
    public void testDoubleStringConstant() throws TwistParseException {
        Expression parsed = new TwistParser("\"foo\"").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals("foo", result);
    }

    @Test
    public void testBooleanConstant() throws TwistParseException {
        Expression parsed = new TwistParser("true").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals(true, result);
        parsed = new TwistParser("false").parseExpression();
        result = parsed.evaluate(new SimpleContext());
        assertEquals(false, result);
    }

    @Test
    public void testArithmeticOperationsOnInts() throws TwistParseException {
        Expression parsed = new TwistParser("7 + 4").parseExpression();
        Object result = parsed.evaluate(new SimpleContext());
        assertEquals(11, result);
        parsed = new TwistParser("19-3").parseExpression();
        result = parsed.evaluate(new SimpleContext());
        assertEquals(16, result);
        parsed = new TwistParser("13*3").parseExpression();
        result = parsed.evaluate(new SimpleContext());
        assertEquals(39, result);
        parsed = new TwistParser("17/4").parseExpression();
        result = parsed.evaluate(new SimpleContext());
        assertEquals(4, result);
    }
}
