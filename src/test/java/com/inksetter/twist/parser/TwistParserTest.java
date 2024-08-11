package com.inksetter.twist.parser;

import com.inksetter.twist.TwistEngine;
import com.inksetter.twist.exec.ExpressionStatement;
import com.inksetter.twist.exec.Statement;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.AssignmentExpression;
import com.inksetter.twist.expression.FunctionExpression;
import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.expression.operators.arith.MultiplyExpression;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TwistParserTest {
    

    @Test
    public void testEmptyScript() {
        try {
            StatementBlock parsed = (StatementBlock) new TwistParser("").parseScript();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (ScriptSyntaxException e) {
            // Normal
        }
    }

    @Test
    public void testAssignment() throws ScriptSyntaxException {
        StatementBlock parsed = (StatementBlock) new TwistParser("a = 100;").parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        assertTrue(statement instanceof ExpressionStatement);
        Expression expr = ((ExpressionStatement) statement).getExpression();
        assertTrue(expr instanceof AssignmentExpression);
    }

    @Test
    public void testMultipleStatements() throws ScriptSyntaxException {
        Map<String, TwistFunction> functions = Map.of("callFunction", args -> null, "print", args -> "PRINT");
        String script =
                "a = 100;\n" +
                "b = callFunction(a, 'String');\n" +
                "if (a == b && c != true) print('WOW');\n";

        StatementBlock parsed = (StatementBlock) new TwistParser(script, new TwistEngine(functions)).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(3, statements.size());
    }

    @Test
    public void testTernaryExpression() throws ScriptSyntaxException {
        StatementBlock parsed = (StatementBlock) new TwistParser("foo = a < 100 ? 'Yes' : 'No'").parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        assertTrue(statement instanceof ExpressionStatement);
        Expression expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof AssignmentExpression);
    }

    @Test
    public void testFunctionCall() throws ScriptSyntaxException {
        Map<String, TwistFunction> functions = Map.of("func", args -> null, "func2", args -> null);

        String script =
                "a = 1000 * func('var' + b);" +
                "func2(a);";
        StatementBlock parsed = (StatementBlock) new TwistParser(script, new TwistEngine(functions)).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(2, statements.size());
        Statement statement = statements.get(0);
        Expression expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof AssignmentExpression);

        statement = statements.get(1);
        expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof FunctionExpression);
    }
}
