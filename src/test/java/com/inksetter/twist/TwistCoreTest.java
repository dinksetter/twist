package com.inksetter.twist;

import com.inksetter.twist.exec.SimpleContext;
import com.inksetter.twist.expression.function.GenericFunction;
import com.inksetter.twist.parser.TwistParseException;
import com.inksetter.twist.parser.TwistParser;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TwistCoreTest {

    @Test
    public void testDateArithmetic() throws TwistParseException {
        MyContext context = new MyContext();
        Instant now = Instant.now();
        Object out = new TwistParser("(now() - 4.4) + 3").parseExpression().evaluate(context);
        assertEquals(TwistDataType.DATETIME, ValueUtils.getType(out));
        assertEquals(Date.class, out.getClass());
    }

    public static class TestClass {
        public String getThing1() { return "aaaa"; }
        public String getThing2() { return "xxxx"; }
    }

    @Test
    public void testProperties() throws TwistParseException{
        MyContext context = new MyContext();
        Map<String, Object> testMap = new HashMap<>();

        testMap.put("a", "a-value");
        testMap.put("b", 3.14);

        TestClass testObj = new TestClass();

        context.setVariable("foo", testMap);
        context.setVariable("bar", testObj);

        String value = ValueUtils.asString(
            new TwistParser("bar.thing1 + ',' + bar.thing2 + ',' + foo.a + ',' + foo.b").parseExpression().evaluate(context)
        );
        assertEquals("aaaa,xxxx,a-value,3.14", value);
    }

    @Test
    public void testJavaMethods() throws TwistParseException {
        MyContext context = new MyContext();
        Map<String, Object> testMap = new HashMap<>();

        testMap.put("a", "abcdefg");

        context.setVariable("foo", testMap);

        String obj = ValueUtils.asString(new TwistParser("foo.a.substring(2,5) + '--' + foo.a.substring(3)").parseExpression().evaluate(context));

        assertEquals("cde--defg", obj);
    }

    @Test
    public void testJavaMethodsWithInvalidArgs() throws TwistParseException {
        MyContext context = new MyContext();
        context.setVariable("foo", "abcd");

        try {
            new TwistParser("foo.substring(2,'900')").parseExpression().evaluate(context);
            Assert.fail("Expected exception");
        }
        catch (TwistException e) {
            // Normal
        }
    }


    public static class ExprTestObject {
        private final String x = "banana";
        private final int y = 23;


        public String getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    @Test
    public void testExpression() throws TwistParseException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        Assert.assertTrue(ValueUtils.asBoolean(new TwistParser("foo.x == 'banana'").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.x != 'banana'").parseExpression().evaluate(context)));
    }


    @Test
    public void testRegexMatch() throws TwistParseException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        Assert.assertTrue(ValueUtils.asBoolean(new TwistParser("foo.x =~ 'b.*'").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.x =~ '.*b'").parseExpression().evaluate(context)));
    }


    @Test
    public void testNumericExpression() throws TwistParseException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        assertEquals(3, ValueUtils.asInt(new TwistParser("foo.y / 7 ").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.y < 19").parseExpression().evaluate(context)));
    }


    @Test
    public void testCustomFunction() throws TwistParseException {
        MyContext context = new MyContext();
        MyResolver resolver = new MyResolver();
        context.setVariable("foo", new ExprTestObject());
        assertEquals("called z([3])", new TwistParser("z(foo.y / 7) ", resolver).parseExpression().evaluate(context));
        assertEquals("called abc([false])", new TwistParser("abc(foo.y < 19)", resolver).parseExpression().evaluate(context));
    }

    private static class MyContext extends SimpleContext {
    }

    private static class MyResolver implements FunctionResolver {
        @Override
        public GenericFunction lookupFunction(String name) {
            return (values) -> "called " + name + "(" + values + ")";
        }
    }
}
