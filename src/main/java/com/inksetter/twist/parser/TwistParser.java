package com.inksetter.twist.parser;

import com.inksetter.twist.Expression;
import com.inksetter.twist.Script;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistEngine;
import com.inksetter.twist.exec.*;
import com.inksetter.twist.expression.*;
import com.inksetter.twist.expression.function.*;
import com.inksetter.twist.expression.operators.AndExpression;
import com.inksetter.twist.expression.operators.IfNullExpression;
import com.inksetter.twist.expression.operators.NotExpression;
import com.inksetter.twist.expression.operators.OrExpression;
import com.inksetter.twist.expression.operators.arith.*;
import com.inksetter.twist.expression.operators.compare.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The core command parser for Twist command syntax.  This parser reads string input
 * and produces an executable CommandSequence tree.
 */
public class TwistParser {
    protected final TwistLexer scan;

    public TwistParser(CharSequence script) {
        this(script, null);
    }

    public TwistParser(CharSequence script, TwistEngine engine) {
        scan = new TwistLexer(script);
    }

    public Script parseScript() throws ScriptSyntaxException {
        scan.next();
        
        StatementBlock seq = buildScript();
        
        if (scan.tokenType() != TwistTokenType.END) {
            throw parseException(TwistTokenType.END);
        }

        return seq;
    }

    public Expression parseExpression() throws ScriptSyntaxException {
        scan.next();
        return buildFullExpression();
    }
    
    //
    // Implementation
    //

    protected StatementBlock buildScript() throws ScriptSyntaxException {
        StatementBlock sequence = new StatementBlock();

        // If empty braces or an empty script occurs, create a single null statement.
        if (scan.tokenType() == TwistTokenType.CLOSE_BRACE || scan.tokenType() == TwistTokenType.END) {
            return sequence;
        }

        Statement statement = buildStatement();
        sequence.addStatement(statement);

        while (scan.tokenType() == TwistTokenType.SEMICOLON || scan.current().getLeadingWhitespace().contains("\n")) {
            // Skip the semicolon. Whitespace rules will take care of the newline end of statement.
            if (scan.tokenType() == TwistTokenType.SEMICOLON) {
                scan.next();
            }

            // Special case -- if someone ends a command sequence with a semicolon, check for reasonable
            // end-of-sequence characters.
            if (scan.tokenType() == TwistTokenType.CLOSE_BRACE || scan.tokenType() == TwistTokenType.END) {
                break;
            }

            statement = buildStatement();
            sequence.addStatement(statement);
        }
        
        return sequence;
    }
    
    protected Expression buildIfExpression() throws ScriptSyntaxException {
        scan.next();
        if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
            throw parseException(TwistTokenType.OPEN_PAREN);
        }
        
        scan.next();
        return buildFullExpression();
    }
    
    protected Statement buildStatement() throws ScriptSyntaxException {
        Statement stmt;

        if (scan.tokenType() == TwistTokenType.RETURN) {
            scan.next();
            Expression returnExpr = buildFullExpression();
            stmt = new ReturnStatement(returnExpr);
        }
        else if (scan.tokenType() == TwistTokenType.IF) {
            Expression ifTest = buildIfExpression();

            if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(TwistTokenType.CLOSE_PAREN);
            }
            scan.next();

            Statement ifTrue = buildStatement();
            Statement ifElse = null;
            if (scan.tokenType() == TwistTokenType.ELSE) {
                scan.next();
                ifElse = buildStatement();
            }
            stmt = new IfStatement(ifTest, ifTrue, ifElse);
        }
        else if (scan.tokenType() == TwistTokenType.TRY) {
            scan.next();

            // We expect braces here, but we want this to be
            if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException(TwistTokenType.OPEN_BRACE);
            }

            // Don't scan the brace.  Let the main block parser do that.
            StatementBlock tryBlock = buildSubSequence();
            List<CatchBlock> catchBlocks = buildCatchBlocks();
            StatementBlock finallyBlock = null;
            if (scan.tokenType() == TwistTokenType.FINALLY) {
                // Scan past the FINALLY keyword
                scan.next();
                finallyBlock = buildSubSequence();
            }

            stmt = new TryStatement(tryBlock, catchBlocks, finallyBlock);
        }
        else if (scan.tokenType() == TwistTokenType.FOR) {
            scan.next();
            if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
                throw parseException(TwistTokenType.OPEN_PAREN);
            }
            scan.next();
            Expression initial = buildFullExpression();

            // Two forms:
            //   - for (x : list-expression) ...
            //   - for (expr; expr; expr) ...
            if (initial instanceof Assignable && scan.tokenType() == TwistTokenType.COLON) {
                scan.next();
                Expression listExpression = buildFullExpression();
                if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(TwistTokenType.CLOSE_PAREN);
                }
                scan.next();

                Statement body = buildStatement();
                stmt = new ForListStatement((Assignable) initial, listExpression, body);
            }
            else {
                if (scan.tokenType() != TwistTokenType.SEMICOLON) {
                    throw parseException(TwistTokenType.SEMICOLON);
                }
                scan.next();
                Expression test = buildFullExpression();
                if (scan.tokenType() != TwistTokenType.SEMICOLON) {
                    throw parseException(TwistTokenType.SEMICOLON);
                }
                scan.next();
                Expression end = buildFullExpression();

                if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(TwistTokenType.CLOSE_PAREN);
                }
                scan.next();

                Statement body = buildStatement();
                stmt = new ForStatement(initial, test, end, body);
            }
        }
        else if (scan.tokenType() == TwistTokenType.DEF) {
            scan.next();
            String functionName = getIdentifier("Function");
            scan.next();
            if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
                throw parseException(TwistTokenType.OPEN_PAREN);
            }
            List<String> argNames = getFunctionArgDef();
            if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException(TwistTokenType.OPEN_BRACE);
            }
            StatementBlock functionBlock = buildSubSequence();

            UserDefFunction newFunc = new UserDefFunction(functionName, argNames, functionBlock);

            stmt = new DefFunctionStatement(functionName, newFunc);
        }
        else if (scan.tokenType() == TwistTokenType.OPEN_BRACE) {
            stmt = new BlockStatement(buildSubSequence());
        }
        else {
            Expression fullExpression = buildFullExpression();
            stmt = new ExpressionStatement(fullExpression);
        }

        return stmt;
    }
    
    protected List<CatchBlock> buildCatchBlocks() throws ScriptSyntaxException {
        if (scan.tokenType() != TwistTokenType.CATCH) {
            return null;
        }

        List<CatchBlock> blocks = new ArrayList<>();
        
        while (scan.tokenType() == TwistTokenType.CATCH) {
            CatchBlock block = new CatchBlock();
            scan.next();
            if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
                throw parseException(TwistTokenType.OPEN_PAREN);
            }
            scan.next();

            block.setType(getIdentifier("TypeName"));
            scan.next();

            block.setVarName(getIdentifier("NAME"));
            scan.next();

            if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(TwistTokenType.CLOSE_PAREN);
            }
            scan.next();
            
            if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException(TwistTokenType.OPEN_BRACE);
            }
            
            scan.next();
            
            // If it is not a close brace then assume it is a sequence
            // If it was a close brace we just leave the sequence empty
            // as in ignoring the exception
            if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                block.setBlock(buildScript());
            }
            
            if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                throw parseException(TwistTokenType.CLOSE_BRACE);
            }

            scan.next();
            
            blocks.add(block);
        }
        return blocks;
    }

    private List<String> getFunctionArgDef() throws ScriptSyntaxException {
        List<String> functionArgs = new ArrayList<>();

        if (scan.tokenType() == TwistTokenType.OPEN_PAREN) {
            scan.next();
            while (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                functionArgs.add(getIdentifier("argName"));
                scan.next();
                if (scan.tokenType() == TwistTokenType.COMMA) {
                    scan.next();
                }
                else if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(TwistTokenType.CLOSE_PAREN);
                }
            }
            scan.next();
        }
        return functionArgs;
    }



    protected StatementBlock buildSubSequence() throws ScriptSyntaxException {
        if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
            throw parseException(TwistTokenType.OPEN_BRACE);
        }

        scan.next();
        StatementBlock seq = buildScript();
        
        if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
            throw parseException(TwistTokenType.CLOSE_BRACE);
        }
        scan.next();
        
        return seq;
    }

    protected Expression buildExpressionTerm() throws ScriptSyntaxException {
        Expression expr = buildExpressionFactor();
        do {
            TwistTokenType operatorToken = scan.tokenType();
            if (operatorToken == TwistTokenType.PLUS) {
                scan.next();
                expr = new PlusExpression(expr, buildExpressionFactor());
            }
            else if (operatorToken == TwistTokenType.MINUS) {
                scan.next();
                expr = new MinusExpression(expr, buildExpressionFactor());
            }
            else {
                break;
            }
        } while (true);
        
        return expr;
    }
    
    protected Expression buildLogicalExpression() throws ScriptSyntaxException {
        Expression expr = buildExpressionTerm();
        TwistTokenType oper = scan.tokenType();
        if (oper == TwistTokenType.EQ) {
            scan.next();
            expr  = new EqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NE) {
            scan.next();
            expr  = new NotEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.GT) {
            scan.next();
            expr  = new GreaterThanExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.GE) {
            scan.next();
            expr  = new GreaterThanOrEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LT) {
            scan.next();
            expr  = new LessThanExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LE) {
            scan.next();
            expr  = new LessThanOrEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LIKE) {
            scan.next();
            expr  = new LikeExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.FIND) {
            scan.next();
            expr  = new RegexFindExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.MATCH) {
            scan.next();
            expr  = new RegexMatchExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NMATCH) {
            scan.next();
            expr  = new RegexNoMatchExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NOT) {
            scan.next();
            if (scan.tokenType() != TwistTokenType.LIKE) {
                throw parseException(TwistTokenType.LIKE);
            }
            scan.next();
            expr  = new NotLikeExpression(expr, buildExpressionTerm());
        }

        // If no operator is present, just return the initial expression
        return expr;
    }
    
    protected Expression buildAndExpression() throws ScriptSyntaxException {
        Expression expr = buildLogicalExpression();
        while (scan.tokenType() == TwistTokenType.AND) {
            scan.next();
            Expression left = expr;
            Expression right = buildLogicalExpression();
            expr = new AndExpression(left, right);
        }
        
        return expr;
    }
    protected Expression buildFullExpression() throws ScriptSyntaxException {
        Expression expr = buildOrExpression();
        if (scan.tokenType() == TwistTokenType.ELVIS) {
            scan.next();
            Expression testExpr = expr;
            Expression elseExpr = buildFullExpression();
            expr = new IfNullExpression(testExpr, elseExpr);
        }
        else if (scan.tokenType() == TwistTokenType.QUESTION) {
            scan.next();
            Expression ternaryIf = expr;
            Expression ternaryThen = buildFullExpression();
            if (scan.tokenType() != TwistTokenType.COLON) {
                throw parseException(TwistTokenType.COLON);
            }
            scan.next();
            Expression ternaryElse = buildFullExpression();
            expr = new TernaryExpression(ternaryIf, ternaryThen, ternaryElse);
        }

        return expr;
    }

    protected Expression buildOrExpression() throws ScriptSyntaxException {
        Expression expr = buildAndExpression();
        while (scan.tokenType() == TwistTokenType.OR) {
            scan.next();
            Expression left = expr;
            Expression right = buildAndExpression();
            expr = new OrExpression(left, right);
        }
        
        return expr;
    }
    
    protected Expression buildExpressionFactor() throws ScriptSyntaxException {
        Expression expr = buildExpressionValue();
        
        do {
            TwistTokenType operatorToken = scan.tokenType();
            
            if (operatorToken == TwistTokenType.STAR) {
                scan.next();
                expr = new MultiplyExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == TwistTokenType.SLASH) {
                scan.next();
                expr = new DivisionExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == TwistTokenType.PERCENT) {
                scan.next();
                expr = new ModExpression(expr, buildExpressionValue());
            }
            else {
                break;
            }
                
        } while (true);
        
        return expr;
    }
    
    protected Expression buildExpressionValue() throws ScriptSyntaxException {
        Expression expr = buildExpressionPossibleValue();
        while (scan.tokenType() == TwistTokenType.DOT || scan.tokenType() == TwistTokenType.OPEN_BRACKET) {
            if (scan.tokenType() == TwistTokenType.DOT) {
                scan.next();
                if (scan.tokenType() != TwistTokenType.IDENTIFIER) {
                    throw parseException(TwistTokenType.IDENTIFIER);
                }
                String identifier = scan.current().getValue();
                scan.next();
                if (scan.tokenType() == TwistTokenType.OPEN_PAREN) {
                    List<Expression> methodArgs = getFunctionArgs();
                    expr = new MethodCallExpression(expr, identifier, methodArgs);
                }
                else {
                    expr = new MemberExpression(expr, identifier);
                }
            }
            else {
                scan.next();
                Expression element = buildFullExpression();
                if (scan.tokenType() != TwistTokenType.CLOSE_BRACKET) {
                    throw parseException(TwistTokenType.CLOSE_BRACKET);
                }
                scan.next();
                expr = new ElementExpression(expr, element);
            }
        }

        if (ASSIGNMENT_OPERS.contains(scan.tokenType())) {
            if (!(expr instanceof Assignable)) {
                throw parseException("illegal assignment");
            }
            TwistTokenType oper = scan.tokenType();
            scan.next();
            switch (oper) {
                case ASSIGNMENT:
                    expr = new AssignmentExpression((Assignable) expr, buildFullExpression());
                    break;
                case PLUSASSIGN:
                    expr = new AssignmentExpression((Assignable) expr, new PlusExpression(expr, buildFullExpression()));
                    break;
                case MINUSASSIGN:
                    expr = new AssignmentExpression((Assignable) expr, new MinusExpression(expr, buildFullExpression()));
                    break;
                case STARASSIGN:
                    expr = new AssignmentExpression((Assignable) expr, new MultiplyExpression(expr, buildFullExpression()));
                    break;
                case SLASHASSIGN:
                    expr = new AssignmentExpression((Assignable) expr, new DivisionExpression(expr, buildFullExpression()));
                    break;
                case PERCENTASSIGN:
                    expr = new AssignmentExpression((Assignable) expr, new ModExpression(expr, buildFullExpression()));
                    break;
                case INCREMENT:
                    expr = new IncrementExpression((Assignable) expr, true);
                    break;
                case DECREMENT:
                    expr = new IncrementExpression((Assignable) expr, false);
                    break;
            }
        }

        return expr;
    }

    protected Expression buildExpressionPossibleValue() throws ScriptSyntaxException {
        boolean isNegative = false;
        switch (scan.tokenType()) {
        case BANG:
            scan.next();
            return new NotExpression(buildExpressionValue());
        case IDENTIFIER:
            String identifier = scan.current().getValue();
            scan.next();
            if (scan.tokenType() == TwistTokenType.OPEN_PAREN) {
                return buildFunctionExpression(identifier);
            }
            else {
                return new ReferenceExpression(identifier);
            }
        case OPEN_PAREN:
            scan.next();
            Expression subExpression = buildFullExpression();
            if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(TwistTokenType.CLOSE_PAREN);
            }
            scan.next();
            return subExpression;
        case MINUS:
            isNegative = true;
            // Pass through
        case PLUS:
            scan.next();
            if (scan.tokenType() != TwistTokenType.NUMBER) {
                throw parseException(TwistTokenType.NUMBER);
            }
            // Pass through
        case NUMBER:
            String numericValue = scan.current().getValue();
            if (isNegative) numericValue = "-" + numericValue;
            Expression numericExpression;
            try {
                if (numericValue.indexOf('.') != -1 ||
                        numericValue.indexOf('e') != -1 ||
                        numericValue.indexOf('E') != -1) {
                    // We've got a floating point value on our hands.
                    numericExpression = new DoubleLiteral(Double.valueOf(numericValue));
                }
                else {
                    // Deal with large numeric values.
                    BigInteger tmpValue = new BigInteger(numericValue, 10);
                    
                    if (tmpValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ||
                        tmpValue.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
                        numericExpression = new DoubleLiteral(Double.valueOf(numericValue));
                    }
                    else {
                        numericExpression = new IntegerLiteral(tmpValue.intValue());
                    }
                }
            }
            catch (NumberFormatException e) {
                throw parseException(TwistTokenType.NUMBER);
            }
            
            scan.next();
            
            return numericExpression;

        case SINGLE_STRING:
        case DOUBLE_STRING:
            String unquoted = scan.current().getValue();
            scan.next();
            return new StringLiteral(dequote(unquoted));

        case MULTI_STRING:
            String rawMultiline = scan.current().getValue();
            scan.next();
            return new StringLiteral(cleanupMultiline(rawMultiline));

        case NULL_TOKEN:
            scan.next();
            return new LiteralExpression(TwistDataType.STRING, null);

        case TRUE:
            scan.next();
            return new LiteralExpression(TwistDataType.BOOLEAN, Boolean.TRUE);

        case FALSE:
            scan.next();
            return new LiteralExpression(TwistDataType.BOOLEAN, Boolean.FALSE);
        case OPEN_BRACE:
            return buildJsonObject();
        case OPEN_BRACKET:
            return buildJsonArray();
        }

        // Now, we've failed to find an expression.
        throw parseException(TwistTokenType.BANG, TwistTokenType.IDENTIFIER, TwistTokenType.OPEN_PAREN,
                TwistTokenType.MINUS, TwistTokenType.PLUS, TwistTokenType.NUMBER,
                TwistTokenType.SINGLE_STRING, TwistTokenType.DOUBLE_STRING, TwistTokenType.MULTI_STRING,
                TwistTokenType.NULL_TOKEN, TwistTokenType.TRUE, TwistTokenType.FALSE, TwistTokenType.OPEN_BRACE,
                TwistTokenType.OPEN_BRACKET);
    }

    protected Expression buildJsonObject() throws ScriptSyntaxException {
        scan.next();
        Map<String, Expression> object = new LinkedHashMap<>();
        if (scan.tokenType() == TwistTokenType.CLOSE_BRACE) {
            scan.next();
        }
        else {
            while (true) {
                String fieldName;
                switch (scan.tokenType()) {
                    case SINGLE_STRING:
                    case DOUBLE_STRING:
                        String unquoted = scan.current().getValue();
                        fieldName = dequote(unquoted);
                        break;
                    case IDENTIFIER:
                        fieldName = scan.current().getValue();
                        break;
                    default:
                        throw parseException(TwistTokenType.SINGLE_STRING);
                }
                scan.next();
                if (scan.tokenType() != TwistTokenType.COLON) {
                    throw parseException(TwistTokenType.COLON);
                }
                scan.next();
                Expression value = buildFullExpression();
                object.put(fieldName, value);

                if (scan.tokenType() == TwistTokenType.CLOSE_BRACE) {
                    scan.next();
                    break;
                }

                if (scan.tokenType() != TwistTokenType.COMMA) {
                    throw parseException(TwistTokenType.COMMA);
                }
                // Skip the comma;
                scan.next();
            }
        }
        return new ObjectExpression(object);
    }
    protected Expression buildJsonArray() throws ScriptSyntaxException {
        scan.next();
        List<Expression> array = new ArrayList<>();
        if (scan.tokenType() == TwistTokenType.CLOSE_BRACKET) {
            scan.next();
        }
        else {
            while (true) {
                Expression value = buildFullExpression();
                array.add(value);

                if (scan.tokenType() == TwistTokenType.CLOSE_BRACKET) {
                    scan.next();
                    break;
                }

                if (scan.tokenType() != TwistTokenType.COMMA) {
                    throw parseException(TwistTokenType.COMMA);
                }
                scan.next();
            }
        }
        return new ArrayExpression(array);
    }


    protected Expression buildFunctionExpression(String functionName) throws ScriptSyntaxException {
        // This method only gets called if parentheses have been seen
        List<Expression> functionArgs = getFunctionArgs();


        return new FunctionExpression(functionName, functionArgs);
    }

    private static final EnumSet<TwistTokenType> ASSIGNMENT_OPERS = EnumSet.of(
            TwistTokenType.ASSIGNMENT, TwistTokenType.PLUSASSIGN, TwistTokenType.MINUSASSIGN,
            TwistTokenType.STARASSIGN, TwistTokenType.SLASHASSIGN, TwistTokenType.PERCENTASSIGN,
            TwistTokenType.INCREMENT, TwistTokenType.DECREMENT);

    private List<Expression> getFunctionArgs() throws ScriptSyntaxException {
        List<Expression> functionArgs = new ArrayList<>();

        if (scan.tokenType() == TwistTokenType.OPEN_PAREN) {
            scan.next();
            while (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                functionArgs.add(buildFullExpression());
                if (scan.tokenType() == TwistTokenType.COMMA) {
                    scan.next();
                }
                else if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(TwistTokenType.CLOSE_PAREN);
                }
            }
            scan.next();
        }
        return functionArgs;
    }

    protected String dequote(String orig) {
        char quotechar = orig.charAt(0);
        int startpos = 1;
        int endpos = orig.length() - 1;
        StringBuilder buf = new StringBuilder(orig.length());
        do {
            int quotepos = orig.indexOf(quotechar, startpos);
            if (quotepos > startpos) {
                buf.append(orig, startpos, quotepos);
            }
            
            if (quotepos < endpos) {
                buf.append(quotechar);
                startpos = quotepos + 2;
            }
            else {
                startpos = quotepos;
            }
        } while (startpos < endpos);
        
        return buf.toString();
    }

    protected String cleanupMultiline(String raw) {
        String temp = raw.substring(3, raw.length() - 3);
        int firstNewline = temp.indexOf('\n');
        if (firstNewline != -1) {
            int c = firstNewline + 1;
            while (c < temp.length() && Character.isWhitespace(temp.charAt(c))) {
                c++;
            }
            String whitespaceToIgnore = temp.substring(firstNewline, c);
            temp = temp.replace(whitespaceToIgnore, "\n");
        }

        if (firstNewline == 0) {
            temp = temp.substring(1);
        }
        return temp;
    }

    protected String getIdentifier(String expect) throws ScriptSyntaxException {
        if (scan.tokenType() == TwistTokenType.IDENTIFIER) {
            return scan.current().getValue();
        }

        throw parseException(TwistTokenType.IDENTIFIER);
    }

    protected ScriptSyntaxException parseException(String problem) {
        return new ScriptSyntaxException(scan, problem);
    }

    protected ScriptSyntaxException parseException(TwistTokenType... expectedTokens) {
        return new UnexpectedTokenException(scan, expectedTokens);
    }
}
