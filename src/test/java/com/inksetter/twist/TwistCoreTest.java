package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.parser.ScriptSyntaxException;
import com.inksetter.twist.parser.TwistParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwistCoreTest {

    private List<String> functionCalls = new ArrayList<>();
    private List<List<Object>> functionArgs = new ArrayList<>();
    private TwistEngine engine = new TwistEngine(Map.of("print", args -> {
        functionCalls.add("print");
        functionArgs.add(args);
        return 3.2;
    }));

    @Test
    public void testMultipleStatements() throws TwistException {
        String script =
                "a = 100;\n" +
                "b = a + 4;\n" +
                "print('WOW ' + b);\n";

        Script parsed = engine.parseScript(script);
        MyContext context = new MyContext();
        parsed.execute(context);
        Assert.assertEquals(100, context.getVariable("a"));
        Assert.assertEquals(104, context.getVariable("b"));
        Assert.assertEquals(1, functionCalls.size());
        Assert.assertEquals(1, functionArgs.size());
        Assert.assertEquals("print", functionCalls.get(0));
        Assert.assertEquals("WOW 104", functionArgs.get(0).get(0));
    }

    @Test
    public void testMultipleStatementsNoSemicolons() throws TwistException {
        String script =
                "a = 100\n" +
                "b = a + 4\n" +
                "print('WOW ' + b)\n";

        Script parsed = engine.parseScript(script);
        MyContext context = new MyContext();
        parsed.execute(context);
        Assert.assertEquals(100, context.getVariable("a"));
        Assert.assertEquals(104, context.getVariable("b"));
        Assert.assertEquals(1, functionCalls.size());
        Assert.assertEquals(1, functionArgs.size());
        Assert.assertEquals("print", functionCalls.get(0));
        Assert.assertEquals("WOW 104", functionArgs.get(0).get(0));
    }

    @Test
    public void testSingleStatementNoSemicolon() throws TwistException {
        String script = "print('WOW ' + (100 + 4))";

        Script parsed = engine.parseScript(script);
        MyContext context = new MyContext();
        parsed.execute(context);
        Assert.assertEquals(1, functionCalls.size());
        Assert.assertEquals(1, functionArgs.size());
        Assert.assertEquals("print", functionCalls.get(0));
        Assert.assertEquals("WOW 104", functionArgs.get(0).get(0));
    }

    @Test
    public void testMultipleStatementsNoSemicolonsNoNewlines() throws TwistException {
        try {
            String script =
                    "a = 100 " +
                            "b = a + 4 " +
                            "print('WOW ' + b)";

            Script parsed = engine.parseScript(script);
            Assert.fail("expected parser error");
        } catch (ScriptSyntaxException e) {
            // Normal
        }
    }

    @Test
    public void testMultipleInvocationsOnTheSameContext() throws TwistException {
        MyContext context = new MyContext();
        Script parsed = engine.parseScript("a = 0");
        parsed.execute(context);
        Script increment = engine.parseScript("a = a + 1");
        for (int i = 0; i < 10000; i++) {
            increment.execute(context);
        }
        Assert.assertEquals(10000, context.getVariable("a"));
    }

    @Test
    public void testDateArithmetic() throws TwistException{
        MyContext context = new MyContext();
        engine.parseScript("a = now(); b = a - 4.4; c = b - a; d = b + 8").execute(context);
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

        engine.parseScript("a = bar.thing1; b = bar.thing2").execute(context);
        engine.parseScript("c = foo.a; d = foo.b").execute(context);

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

        engine.parseScript("aaa = foo.a; bbb = foo.a.substring(2,5); ccc = aaa.substring(3)").execute(context);

        Assert.assertEquals("abcdefg", context.getVariable("aaa"));
        Assert.assertEquals("cde", context.getVariable("bbb"));
        Assert.assertEquals("defg", context.getVariable("ccc"));
    }

    @Test
    public void testJavaMethodsWithInvalidArgs() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", "abcd");

        try {
            engine.parseScript("aaa = foo.substring(2,'900');").execute(context);
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

        engine.parseScript("aaa = json(foo); bbb = aaa.a;").execute(context);
        Assert.assertEquals(900, context.getVariable("bbb"));
    }

    @Test
    public void testRawJson() throws TwistException {
        MyContext context = new MyContext();

        engine.parseScript("aaa = {'a':900}; bbb = aaa.a;").execute(context);
        Assert.assertEquals(900, context.getVariable("bbb"));
        Object aaa = context.getVariable("aaa");
        Assert.assertTrue(aaa instanceof Map);
        Assert.assertEquals(900, ((Map<String, Object>)aaa).get("a"));
        Assert.assertEquals(900, context.getVariable("bbb"));
    }

    @Test
    public void testComplexJson() throws TwistException {
        MyContext context = new MyContext();

        String script = """
                aaa = {
                   "version":1.0,
                   "data":[
                       {
                           "bookId": "0001",
                           "title": "my book",
                           "isbn": "fdsja910321"
                       },
                       {
                           "bookId": "0002",
                           "title": "my book too",
                           "isbn": "fdsja910311"
                       },
                       {
                           "bookId": "0009",
                           "title": "where the wild thing's are",
                           "isbn": "abchzzz0123"
                       }
                   ],
                   "transactionId": 902,
                   "timestamp": "2024-05-06T07:03:09"
                };
                b = aaa.data[2].isbn + "/" + aaa.version; 
                """;

        engine.parseScript(script).execute(context);
        Assert.assertEquals("abchzzz0123/1", context.getVariable("b"));
    }

    @Test
    public void testRawJsonArray() throws TwistException {
        MyContext context = new MyContext();

        engine.parseScript("zurg = 'hello'; aaa = [1,2,3,4,5]; bbb = ['a','b','grumph', zurg]").execute(context);
        Object aaa = context.getVariable("aaa");
        Assert.assertTrue(aaa instanceof List);
        Object bbb = context.getVariable("bbb");
        Assert.assertTrue(bbb instanceof List);
        Assert.assertEquals(2, ((List<Object>)aaa).get(1));
        Assert.assertEquals("hello", ((List<Object>)bbb).get(3));
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


    @Test
    public void testInternalMethods() throws TwistException {
        MyContext context = new MyContext();
        context.setVariable("foo", new ExprTestObject());
        context.setVariable("bar", "spacey string   ");
        Assert.assertEquals("nana", ValueUtils.asString(new TwistParser("foo.x.substring(2)").parseExpression().evaluate(context)));
        Assert.assertEquals("spacey string", ValueUtils.asString(new TwistParser("bar.strip()").parseExpression().evaluate(context)));
    }

    private static class MyContext extends SimpleScriptContext {
    }
}
