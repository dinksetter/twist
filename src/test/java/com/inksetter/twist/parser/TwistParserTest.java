package com.inksetter.twist.parser;

import com.inksetter.twist.exec.ExpressionStatement;
import com.inksetter.twist.exec.Statement;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.AssignmentExpression;
import com.inksetter.twist.expression.FunctionExpression;
import com.inksetter.twist.expression.ReferenceExpression;
import com.inksetter.twist.expression.StringLiteral;
import com.inksetter.twist.expression.function.TwistFunction;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TwistParserTest {
    

    @Test
    public void testEmptyScript() {
        testPrematureEndOfScript("");
    }

    @Test
    public void testPrematureEnd() {
        testPrematureEndOfScript("x = ");
        testPrematureEndOfScript("700 +");
        testPrematureEndOfScript("a = { x :");
        testPrematureEndOfScript("b = function(\"abc\", ");
    }

    @Test
    public void testUnfinishedString() {
        String script = "'unmatched";
        ScriptSyntaxException e = assertThrows(ScriptSyntaxException.class, () -> new TwistParser(script).parseScript());
        assertEquals(script.length(), e.getPos());
    }

    @Test
    public void testUnfinishedComment() {
        String script = "/* comment";
        ScriptSyntaxException e = assertThrows(ScriptSyntaxException.class, () -> new TwistParser(script).parseScript());
        assertEquals(script.length(), e.getPos());
    }


    private void testPrematureEndOfScript(String script) {
        UnexpectedTokenException e = assertThrows(UnexpectedTokenException.class, () -> new TwistParser(script).parseScript());
        assertEquals(script.length(), e.getPos());
        assertEquals(TwistTokenType.END, e.getToken());
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
        Map<String, TwistFunction> functions = Map.of("callFunction", (args, context) -> null, "print", (args, context) -> "PRINT");
        String script =
                "a = 100;\n" +
                "b = callFunction(a, 'String');\n" +
                "if (a == b && c != true) print('WOW');\n";

        StatementBlock parsed = (StatementBlock) new TwistParser(script).parseScript();
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
        Map<String, TwistFunction> functions = Map.of("func", (args, context) -> null, "func2", (args, context) -> null);

        String script =
                "a = 1000 * func('var' + b);" +
                "func2(a);";
        StatementBlock parsed = (StatementBlock) new TwistParser(script).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(2, statements.size());
        Statement statement = statements.get(0);
        Expression expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof AssignmentExpression);

        statement = statements.get(1);
        expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof FunctionExpression);
    }

    @Test
    public void testStringLiterals() throws ScriptSyntaxException {
        validateStringLiteral("\"\"", "");
        validateStringLiteral("\"hell's kitchen\"", "hell's kitchen");
        validateStringLiteral("'hell''s kitchen'", "hell's kitchen");
        validateStringLiteral("\"a\nb\nc\"", "a\nb\nc");
    }
    @Test
    public void testMultilineString() throws ScriptSyntaxException {
        validateStringLiteral("\"\"\"\"\"\"", "");
        validateStringLiteral("\"\"\" hello\"\"\"", " hello");
        validateStringLiteral("\"\"\"\n" +
                "    this\n" +
                "    is\n" +
                "    a\n" +
                "    test\n" +
                "    \"\"\"\n", "this\nis\na\ntest\n");
        validateStringLiteral("\"\"\"\n" +
                        "        line1\n" +
                        "\tline2\n" +
                        "        line3\n" +
                        "        \"\"\"\n",
                "line1\n\tline2\nline3\n"
                );
    }

    private void validateStringLiteral(String raw, String expected) throws ScriptSyntaxException {
        StatementBlock parsed = (StatementBlock) new TwistParser(raw).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        Statement statement = statements.get(0);
        Expression expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof StringLiteral);
        String result = ((StringLiteral) expr).evaluate(null);

        assertEquals(expected, result);
    }

    @Test
    public void testBareWordFunction() throws ScriptSyntaxException {
        Map<String, TwistFunction> functions = Map.of(
                "func", (args, context) -> "call [" + args + "]",
                "func2", (args, context) -> "call2 [" + args + "]");
        String script = "func\n" +
                "func2(1,2,3)";
        StatementBlock parsed = (StatementBlock) new TwistParser(script).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(2, statements.size());
        Statement statement = statements.get(0);
        Expression expr = ((ExpressionStatement)statement).getExpression();
        assertTrue(expr instanceof ReferenceExpression);
        Statement statement2 = statements.get(1);
        Expression expr2 = ((ExpressionStatement)statement2).getExpression();
        assertTrue(expr2 instanceof FunctionExpression);
    }
}
