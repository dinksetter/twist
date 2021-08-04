package com.inksetter.twist.parser;

import com.inksetter.twist.exec.ExecutableStatement;
import com.inksetter.twist.exec.StatementSequence;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TwistParserTest {
    

    @Test
    public void testEmptyScript() throws TwistParseException {
        try {
            StatementSequence parsed = new TwistParser("").parse();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (TwistParseException e) {
            // Normal
        }
    }

    @Test
    public void testAssignment() throws TwistParseException {
        StatementSequence parsed = new TwistParser("a = 100;").parse();
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

        StatementSequence parsed = new TwistParser(script).parse();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(3, statements.size());
    }

    @Test
    public void testTernaryExpression() throws TwistParseException {
        StatementSequence parsed = new TwistParser("foo = a < 100 ? 'Yes' : 'No'").parse();
        List<ExecutableStatement> statements = parsed.getStatements();
        assertEquals(1, statements.size());
        ExecutableStatement statement = statements.get(0);
        String assignTo = statement.getAssignment();
        assertEquals("foo", assignTo);
    }
}
