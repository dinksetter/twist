package com.inksetter.twist.parser;

import com.inksetter.twist.exec.ExpressionStatement;
import com.inksetter.twist.exec.NullStatement;
import com.inksetter.twist.exec.Statement;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.AssignmentExpression;
import com.inksetter.twist.expression.CallExpression;
import com.inksetter.twist.expression.LambdaExpression;
import com.inksetter.twist.expression.FunctionExpression;
import com.inksetter.twist.expression.ReferenceExpression;
import com.inksetter.twist.expression.StringLiteral;
import com.inksetter.twist.expression.function.TwistFunction;
import org.junit.Test;

import com.inksetter.twist.MapContext;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TwistParserTest {

    @Test
    public void testEmptyScript() throws ScriptSyntaxException {
        StatementBlock parsed = (StatementBlock) new TwistParser("").parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(0, statements.size());
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
    public void testSingleLineComment() throws ScriptSyntaxException {
        String script = """
                // blah blah blah
                // more blah
                """;
        StatementBlock parsed = (StatementBlock) new TwistParser(script).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(0, statements.size());
    }

    @Test
    public void testSingleLineCommentNoNewline() throws ScriptSyntaxException {
        String script = "// blah blah";
        StatementBlock parsed = (StatementBlock) new TwistParser(script).parseScript();
        List<Statement> statements = parsed.getStatements();
        assertEquals(0, statements.size());
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
    public void testQuoteEscapes() throws ScriptSyntaxException {
        validateStringLiteral("\"say \"\"hi\"\"\"", "say \"hi\"");
        validateStringLiteral("'it''s'", "it's");
        // No backslash escapes
        validateStringLiteral("'a\\nb'", "a\\nb");
    }

    @Test
    public void testSingleQuotedMultilineString() throws ScriptSyntaxException {
        validateStringLiteral("'''\n    one\n    two'''", "one\ntwo");
    }

    @Test
    public void testNumericLiterals() throws com.inksetter.twist.TwistException {
        assertEquals(42, new TwistParser("42").parseExpression().evaluate(null));
        assertEquals(3.14, new TwistParser("3.14").parseExpression().evaluate(null));
        assertEquals(1000.0, new TwistParser("1e3").parseExpression().evaluate(null));
        assertEquals(1500.0, new TwistParser("1.5E3").parseExpression().evaluate(null));
        assertEquals(Integer.MIN_VALUE, new TwistParser(String.valueOf(Integer.MIN_VALUE)).parseExpression().evaluate(null));
        assertEquals(3.0e9, new TwistParser("3000000000").parseExpression().evaluate(null));
        assertEquals(-3.0e9, new TwistParser("-3000000000").parseExpression().evaluate(null));
    }

    @Test
    public void testUnaryMinus() throws com.inksetter.twist.TwistException {
        assertEquals(-5, new TwistParser("-x").parseExpression().evaluate(new MapContext(Map.of("x", 5))));
        assertEquals(-5, new TwistParser("-(x)").parseExpression().evaluate(new MapContext(Map.of("x", 5))));
        // A plus sign is only allowed in front of a number
        assertThrows(UnexpectedTokenException.class, () -> new TwistParser("+'a'").parseExpression());
    }

    @Test
    public void testKeywordsAreNotIdentifiers() {
        for (String keyword : List.of("if", "else", "for", "try", "catch", "finally", "def", "return")) {
            assertThrows(keyword, ScriptSyntaxException.class, () -> new TwistParser(keyword + " = 1").parseScript());
        }
    }

    @Test
    public void testIdentifiers() throws com.inksetter.twist.TwistException {
        assertEquals(1, new TwistParser("_x1 = 1; _x1").parseScript().execute(new com.inksetter.twist.SimpleScriptContext()));
    }

    @Test
    public void testMalformedStatements() {
        List<String> scripts = List.of(
                "if x > 1 { }",
                "if (x > 1 { }",
                "for x : [1] { }",
                "for (x : [1] { }",
                "for (i = 0; i < 10) { }",
                "for (i = 0 i < 10; i++) { }",
                "for (1 : [1]) { }",
                "try x = 1",
                "try { } catch Exception e { }",
                "try { } catch (Exception) { }",
                "try { } catch (Exception e) x",
                "try { } finally x",
                "def f(a b) { }",
                "{ a = 1",
                "a = 1 }",
                "a = [1, 2",
                "a = [1 2]",
                "a = {x 1}",
                "a = {x: 1 y: 2}",
                "a = {1: 2}",
                "a.1",
                "a[1",
                "a ? b",
                "f(a b)");
        for (String script : scripts) {
            assertThrows(script, ScriptSyntaxException.class, () -> new TwistParser(script).parseScript());
        }
    }

    @Test
    public void testErrorPosition() {
        ScriptSyntaxException e = assertThrows(ScriptSyntaxException.class,
                () -> new TwistParser("a = 1\nb = (2 + 3\nc = 4").parseScript());
        assertTrue(e.getPos() > "a = 1\nb = (2 + 3".length());
    }

    @Test
    public void testEmptyBlocks() throws ScriptSyntaxException {
        new TwistParser("{}").parseScript();
        new TwistParser("if (true) {} else {}").parseScript();
        new TwistParser("for (x : []) {}").parseScript();
        new TwistParser("try {} catch (Exception e) {} finally {}").parseScript();
        new TwistParser("def f() {}").parseScript();
        new TwistParser("f = -> {}").parseScript();
    }

    @Test
    public void testStatementSeparators() throws ScriptSyntaxException {
        assertEquals(3, ((StatementBlock) new TwistParser("a = 1; b = 2; c = 3").parseScript()).getStatements().size());
        assertEquals(3, ((StatementBlock) new TwistParser("a = 1\nb = 2\nc = 3").parseScript()).getStatements().size());
        assertEquals(2, ((StatementBlock) new TwistParser("a = 1;\n\nb = 2;").parseScript()).getStatements().size());
        // Empty statements are not allowed
        assertThrows(ScriptSyntaxException.class, () -> new TwistParser("a = 1;;b = 2").parseScript());
        // An expression can continue across lines after a binary operator
        assertEquals(1, ((StatementBlock) new TwistParser("a = 1 +\n  2").parseScript()).getStatements().size());
    }

    @Test
    public void testNewlineBeforeParenContinuesExpression() throws ScriptSyntaxException {
        // Documented behavior: a line starting with ( or [ continues the previous expression
        assertEquals(1, ((StatementBlock) new TwistParser("x = a\n(b + 1)").parseScript()).getStatements().size());
        assertEquals(1, ((StatementBlock) new TwistParser("x = a\n[0]").parseScript()).getStatements().size());
        assertEquals(2, ((StatementBlock) new TwistParser("x = a;\n(b + 1)").parseScript()).getStatements().size());
    }

    @Test
    public void testLambdaParses() throws ScriptSyntaxException {
        Expression expr = new TwistParser("-> (a, b) { a + b }").parseExpression();
        assertTrue(expr instanceof LambdaExpression);
        assertTrue(new TwistParser("-> { 1 }").parseExpression() instanceof LambdaExpression);
        assertTrue(new TwistParser("(-> (a) { a })(1)").parseExpression() instanceof CallExpression);
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
