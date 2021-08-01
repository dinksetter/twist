package com.inksetter.twist;

import com.inksetter.twist.exec.AbstractContext;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.exec.ExecutableStatement;
import com.inksetter.twist.exec.StatementSequence;
import com.inksetter.twist.parser.TwistParseException;
import com.inksetter.twist.parser.TwistParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        Assert.assertEquals(100, context.getVariable("a").getValue());
        Assert.assertEquals(104, context.getVariable("b").getValue());
        Assert.assertEquals(1, context._functionCalls.size());
        Assert.assertEquals(1, context._functionArgs.size());
        Assert.assertEquals("print", context._functionCalls.get(0));
        Assert.assertEquals("WOW 104", context._functionArgs.get(0).get(0).getValue());
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
        Assert.assertEquals(10000, context.getVariable("a").getValue());
    }
    private static class MyContext extends AbstractContext {

        private final List<String> _functionCalls = new ArrayList<>();
        private final List<List<TwistValue>> _functionArgs = new ArrayList<>();

        @Override
        public TwistValue invokeExternalFunction(String functionName, List<TwistValue> argValues) {
            _functionCalls.add(functionName);
            _functionArgs.add(argValues);
            return new TwistValue(-3.2);
        }

        @Override
        public boolean lookupExternalFunction(String functionName) {
            return true;
        }
    }
}
