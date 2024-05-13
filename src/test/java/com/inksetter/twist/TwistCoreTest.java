package com.inksetter.twist;

import com.inksetter.twist.exec.AbstractContext;
import com.inksetter.twist.exec.StatementSequence;
import com.inksetter.twist.parser.TwistParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwistCoreTest {
    @Test
    public void testMutlipleStatements() throws TwistException {
        String script =
                "a = 100;\n" +
                "b = a + 4;\n" +
                "print('WOW ' + b);\n";

        StatementSequence parsed = new TwistParser(script).parse();
        MyContext context = new MyContext();
        parsed.execute(context, false);
        Assert.assertEquals(100, context.getVariable("a"));
        Assert.assertEquals(104, context.getVariable("b"));
        Assert.assertEquals(1, context._functionCalls.size());
        Assert.assertEquals(1, context._functionArgs.size());
        Assert.assertEquals("print", context._functionCalls.get(0));
        Assert.assertEquals("WOW 104", context._functionArgs.get(0).get(0));
    }

    @Test
    public void testMultipleInvocationsOnTheSameContext() throws TwistException {
        MyContext context = new MyContext();
        StatementSequence parsed = new TwistParser("a = 0").parse();
        parsed.execute(context, false);
        StatementSequence increment = new TwistParser("a = a + 1").parse();
        for (int i = 0; i < 10000; i++) {
            increment.execute(context, false);
        }
        Assert.assertEquals(10000, context.getVariable("a"));
    }

    @Test
    public void testDateArithmetic() throws TwistException{
        MyContext context = new MyContext();
        new TwistParser("a = now(); b = a - 4.4; c = b - a; d = b + 8").parse().execute(context, false);
        System.out.println(context.getVariable("a"));
        System.out.println(context.getVariable("b"));
        System.out.println(context.getVariable("c"));
        System.out.println(context.getVariable("d"));
    }

    public static class TestClass {
        public String getThing1() { return "aaaa"; }
        public String getThing2() { return "xxxx"; }
    }

    @Test
    public void testProperties() throws TwistException{
        MyContext context = new MyContext();
        Map<String, Object> testMap = new HashMap<>();

        testMap.put("a", "a-value");
        testMap.put("b", 3.14);

        TestClass testObj = new TestClass();

        context.setVariable("foo", testMap);
        context.setVariable("bar", testObj);

        new TwistParser("a = bar.thing1; b = bar.thing2").parse().execute(context, false);
        new TwistParser("c = foo.a; d = foo.b").parse().execute(context, false);

        Assert.assertEquals("aaaa", context.getVariable("a"));
        Assert.assertEquals("xxxx", context.getVariable("b"));
        Assert.assertEquals("a-value", context.getVariable("c"));
        Assert.assertEquals(3.14, context.getVariable("d"));
    }

    @Test
    public void testJavaMethods() throws TwistException {
        MyContext context = new MyContext();
        Map<String, Object> testMap = new HashMap<>();

        testMap.put("a", "abcdefg");

        context.setVariable("foo", testMap);

        new TwistParser("aaa = foo.a; bbb = foo.a.substring(2,5); ccc = aaa.substring(3)").parse().execute(context, false);

        Assert.assertEquals("abcdefg", context.getVariable("aaa"));
        Assert.assertEquals("cde", context.getVariable("bbb"));
        Assert.assertEquals("defg", context.getVariable("ccc"));
    }

    @Test
    public void testJavaMethodsWithInvalidArgs() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", "abcd");

        try {
            new TwistParser("aaa = foo.substring(2,'900');").parse().execute(context, false);
            Assert.fail("Expected exception");
        }
        catch (TwistException e) {
            // Normal
        }
    }

    @Test
    public void testJson() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", "{\"a\": 900}");

        new TwistParser("aaa = json(foo); bbb = aaa.a;").parse().execute(context, false);
        Assert.assertEquals(900, context.getVariable("bbb"));
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
    public void testExpression() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        Assert.assertTrue(ValueUtils.asBoolean(new TwistParser("foo.x == 'banana'").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.x != 'banana'").parseExpression().evaluate(context)));
    }


    @Test
    public void testRegexMatch() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        Assert.assertTrue(ValueUtils.asBoolean(new TwistParser("foo.x =~ 'b.*'").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.x =~ '.*b'").parseExpression().evaluate(context)));
    }


    @Test
    public void testNumericExpression() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        Assert.assertEquals(3, ValueUtils.asInt(new TwistParser("foo.y / 7 ").parseExpression().evaluate(context)));
        Assert.assertFalse(ValueUtils.asBoolean(new TwistParser("foo.y < 19").parseExpression().evaluate(context)));
    }

    private static class MyContext extends AbstractContext {

        private final List<String> _functionCalls = new ArrayList<>();
        private final List<List<Object>> _functionArgs = new ArrayList<>();

        @Override
        public Object invokeExternalFunction(String functionName, List<Object> argValues) {
            _functionCalls.add(functionName);
            _functionArgs.add(argValues);
            return -3.2;
        }

        @Override
        public boolean lookupExternalFunction(String functionName) {
            return true;
        }
    }
}
