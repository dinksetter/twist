package com.inksetter.twist.parser;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.exec.CatchBlock;
import com.inksetter.twist.exec.ExecutableStatement;
import com.inksetter.twist.exec.ExecutableScript;
import com.inksetter.twist.expression.*;
import com.inksetter.twist.expression.operators.AndExpression;
import com.inksetter.twist.expression.operators.IfNullExpression;
import com.inksetter.twist.expression.operators.NotExpression;
import com.inksetter.twist.expression.operators.OrExpression;
import com.inksetter.twist.expression.operators.arith.*;
import com.inksetter.twist.expression.operators.compare.*;

import java.math.BigInteger;
import java.util.ArrayList;
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
        scan = new TwistLexer(script);
    }
    
    public ExecutableScript parseScript() throws TwistParseException {
        scan.next();
        
        ExecutableScript seq = buildScript();
        
        if (scan.tokenType() != TwistTokenType.END) {
            throw new TwistParseException(scan.getLine() + 1, scan.getLinePos() + 1, "Unexpected token: " + scan.current());
        }

        return seq;
    }

    public Expression parseExpression() throws TwistParseException{
        scan.next();
        return buildFullExpression();
    }
    
    //
    // Implementation
    //

    protected ExecutableScript buildScript() throws TwistParseException {
        ExecutableScript sequence = new ExecutableScript();
        
        ExecutableStatement statement = buildStatement();
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
    
    protected Expression buildIfExpression() throws TwistParseException {
        scan.next();
        if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
            throw parseException("(");
        }
        
        scan.next();
        return buildFullExpression();
    }
    
    protected ExecutableStatement buildStatement() throws TwistParseException {
        ExecutableStatement stmt = new ExecutableStatement();

        if (scan.tokenType() == TwistTokenType.IF) {
            stmt.setIfTest(buildIfExpression());

            if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            scan.next();

            stmt.setIfStatement(buildStatement());

            if (scan.tokenType() == TwistTokenType.ELSE) {
                scan.next();
                stmt.setElseStatement(buildStatement());
            }
        }
        else if (scan.tokenType() == TwistTokenType.TRY) {
            scan.next();

            // We expect braces here, but we want this to be
            if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException("{");
            }

            // Don't scan the brace.  Let the main block parser do that.
            stmt.setSubSequence(buildSubSequence());
            stmt.setCatchBlocks(buildCatchBlocks());
            if (scan.tokenType() == TwistTokenType.FINALLY) {
                // Scan past the FINALLY keyword
                scan.next();
                stmt.setFinallyBlock(buildSubSequence());
            }
        }
        else if (scan.tokenType() == TwistTokenType.FOR) {
//            stmt.setForSequence(buildForSequence());
        }
        else {
            if (scan.tokenType() == TwistTokenType.OPEN_BRACE) {
                stmt.setSubSequence(buildSubSequence());
            }
            else {
                if (scan.tokenType() == TwistTokenType.IDENTIFIER) {
                    String identifier = getIdentifier("NAME");
                    TwistLexer.TwistToken save = scan.current();
                    scan.next();
                    while (scan.tokenType() == TwistTokenType.DOT) {
                        scan.next();
                        if (scan.tokenType() == TwistTokenType.IDENTIFIER) {

                        }
                    }

                    if (scan.tokenType() == TwistTokenType.ASSIGNMENT) {
                        // Assignment
                        scan.next();
                        stmt.setAssignment(identifier);
                    }
                    else {
                        scan.reset(save);
                    }
                }
                stmt.setExpression(buildFullExpression());
            }
        }

        return stmt;
    }
    
    protected List<CatchBlock> buildCatchBlocks() throws TwistParseException {
        if (scan.tokenType() != TwistTokenType.CATCH) {
            return null;
        }

        List<CatchBlock> blocks = new ArrayList<>();
        
        while (scan.tokenType() == TwistTokenType.CATCH) {
            CatchBlock block = new CatchBlock();
            scan.next();
            if (scan.tokenType() != TwistTokenType.OPEN_PAREN) {
                throw parseException("(");
            }
            scan.next();

            block.setType(getIdentifier("TypeName"));
            scan.next();

            block.setVarName(getIdentifier("NAME"));
            scan.next();

            if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            scan.next();
            
            if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException("{");
            }
            
            scan.next();
            
            // If it is not a close brace then assume it is a sequence
            // If it was a close brace we just leave the sequence empty
            // as in ignoring the exception
            if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                block.setBlock(buildScript());
            }
            
            if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                throw parseException("}");
            }

            scan.next();
            
            blocks.add(block);
        }
        return blocks;
    }
    
    protected ExecutableScript buildSubSequence() throws TwistParseException {
        if (scan.tokenType() != TwistTokenType.OPEN_BRACE) {
            throw parseException("{");
        }

        scan.next();
        ExecutableScript seq = buildScript();
        
        if (scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
            throw parseException("}");
        }
        scan.next();
        
        return seq;
    }

    protected Expression buildExpressionTerm() throws TwistParseException {
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
    
    protected Expression buildLogicalExpression() throws TwistParseException {
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
        else if (oper == TwistTokenType.MATCH) {
            scan.next();
            expr  = new RegexMatchExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NOT) {
            scan.next();
            if (scan.tokenType() != TwistTokenType.LIKE) {
                throw parseException("LIKE");
            }
            scan.next();
            expr  = new NotLikeExpression(expr, buildExpressionTerm());
        }

        // If no operator is present, just return the initial expression
        return expr;
    }
    
    protected Expression buildAndExpression() throws TwistParseException {
        Expression expr = buildLogicalExpression();
        while (scan.tokenType() == TwistTokenType.AND) {
            scan.next();
            Expression left = expr;
            Expression right = buildLogicalExpression();
            expr = new AndExpression(left, right);
        }
        
        return expr;
    }
    protected Expression buildFullExpression() throws TwistParseException {
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
                throw parseException(":");
            }
            scan.next();
            Expression ternaryElse = buildFullExpression();
            expr = new TernaryExpression(ternaryIf, ternaryThen, ternaryElse);
        }

        return expr;
    }

    protected Expression buildOrExpression() throws TwistParseException {
        Expression expr = buildAndExpression();
        while (scan.tokenType() == TwistTokenType.OR) {
            scan.next();
            Expression left = expr;
            Expression right = buildAndExpression();
            expr = new OrExpression(left, right);
        }
        
        return expr;
    }
    
    protected Expression buildExpressionFactor() throws TwistParseException {
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
    
    protected Expression buildExpressionValue() throws TwistParseException {
        Expression expr = buildExpressionPossibleValue();
        while (scan.tokenType() == TwistTokenType.DOT || scan.tokenType() == TwistTokenType.OPEN_BRACKET) {
            if (scan.tokenType() == TwistTokenType.DOT) {
                scan.next();
                if (scan.tokenType() != TwistTokenType.IDENTIFIER) {
                    throw parseException("identifier");
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
                    throw parseException("]");
                }
                scan.next();
                expr = new ElementExpression(expr, element);
            }
        }

        return expr;
    }

    protected Expression buildExpressionPossibleValue() throws TwistParseException {
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
                throw parseException(")");
            }
            scan.next();
            return subExpression;
        case MINUS:
            isNegative = true;
            // Pass through
        case PLUS:
            scan.next();
            if (scan.tokenType() != TwistTokenType.NUMBER) {
                throw parseException("<NUMBER>");
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
                e.printStackTrace();
                throw parseException("NUMERIC");
            }
            
            scan.next();
            
            return numericExpression;

        case SINGLE_STRING:
        case DOUBLE_STRING:
            String unquoted = scan.current().getValue();
            scan.next();
            return new StringLiteral(dequote(unquoted));
            
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
        throw parseException("expression");
    }

    protected Expression buildJsonObject() throws TwistParseException {
        scan.next();
        Map<String, Expression> object = new LinkedHashMap<>();
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
                    throw parseException("field");
            }
            scan.next();
            if (scan.tokenType() != TwistTokenType.COLON) {
                throw parseException("colon");
            }
            scan.next();
            Expression value = buildFullExpression();
            object.put(fieldName, value);

            if (scan.tokenType() == TwistTokenType.CLOSE_BRACE) {
                scan.next();
                break;
            }

            if (scan.tokenType() != TwistTokenType.COMMA) {
                throw parseException("comma");
            }

            // Skip the comma;
            scan.next();
        }
        return new ObjectExpression(object);
    }
    protected Expression buildJsonArray() throws TwistParseException {
        scan.next();
        List<Expression> array = new ArrayList<>();
        while (true) {
            Expression value = buildFullExpression();
            array.add(value);

            if (scan.tokenType() == TwistTokenType.CLOSE_BRACKET) {
                scan.next();
                break;
            }

            if (scan.tokenType() != TwistTokenType.COMMA) {
                throw parseException("comma");
            }
            scan.next();
        }
        return new ArrayExpression(array);
    }


    protected Expression buildFunctionExpression(String functionName) throws TwistParseException {
        // This method only gets called if parentheses have been seen
        List<Expression> functionArgs = getFunctionArgs();

        return FunctionExpression.chooseFunction(functionName, functionArgs);
    }

    private List<Expression> getFunctionArgs() throws TwistParseException {
        List<Expression> functionArgs = new ArrayList<>();

        if (scan.tokenType() == TwistTokenType.OPEN_PAREN) {
            scan.next();
            while (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                functionArgs.add(buildFullExpression());
                if (scan.tokenType() == TwistTokenType.COMMA) {
                    scan.next();
                }
                else if (scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(")");
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

    protected String getIdentifier(String expect) throws TwistParseException {
        if (scan.tokenType() == TwistTokenType.IDENTIFIER) {
            return scan.current().getValue();
        }

        throw parseException(expect);
    }
    
    protected TwistParseException parseException(String expected) {
        return new TwistParseException(scan.getLine() + 1, scan.getLinePos() + 1,
                "Expected: " + expected + ", got " + scan.current());
    }

}
