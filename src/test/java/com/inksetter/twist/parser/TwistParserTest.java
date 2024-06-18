package com.inksetter.twist.parser;

import com.inksetter.twist.exec.ExecutableStatement;
import com.inksetter.twist.exec.ExecutableScript;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.FunctionExpression;
import com.inksetter.twist.expression.operators.arith.MultiplyExpression;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TwistParserTest {
    

    @Test
    public void testEmptyScript() {
        try {
            ExecutableScript parsed = new TwistParser("").parseScript();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (TwistParseException e) {
            // Normal
        }
    }

    @Test
    public void testAssignment() throws TwistParseException {
        ExecutableScript parsed = new TwistParser("a = 100;").parseScript();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        ExecutableStatement statement = statements.get(0);
        String assignTo = statement.getAssignment();
        assertEquals("a", assignTo);
    }

    @Test
    public void testMutlipleStatements() throws TwistParseException {
        String script =
                "a = 100;\n" +
                "b = callFunction(a, 'String');\n" +
                "if (a == b && c != true) print('WOW');\n";

        ExecutableScript parsed = new TwistParser(script).parseScript();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(3, statements.size());
    }

    @Test
    public void testTernaryExpression() throws TwistParseException {
        ExecutableScript parsed = new TwistParser("foo = a < 100 ? 'Yes' : 'No'").parseScript();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        ExecutableStatement statement = statements.get(0);
        String assignTo = statement.getAssignment();
        assertEquals("foo", assignTo);
    }

    @Test
    public void testFunctionCall() throws TwistParseException {
        String script =
                "a = 1000 * func('var' + b);" +
                "func2(a);";
        ExecutableScript parsed = new TwistParser(script).parseScript();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(2, statements.size());
        ExecutableStatement statement = statements.get(0);
        String assignTo = statement.getAssignment();
        assertEquals("a", assignTo);
        Expression expr = statement.getExpression();
        assertTrue(expr instanceof MultiplyExpression);
        statement = statements.get(1);
        expr = statement.getExpression();
        assertTrue(expr instanceof FunctionExpression);
    }
}
