package com.inksetter.twist.parser;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.exec.CatchBlock;
import com.inksetter.twist.exec.ExecutableStatement;
import com.inksetter.twist.exec.StatementSequence;
import com.inksetter.twist.expression.DoubleLiteral;
import com.inksetter.twist.expression.ElementExpression;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.FunctionExpression;
import com.inksetter.twist.expression.IntegerLiteral;
import com.inksetter.twist.expression.LiteralExpression;
import com.inksetter.twist.expression.MemberExpression;
import com.inksetter.twist.expression.MethodCallExpression;
import com.inksetter.twist.expression.ReferenceExpression;
import com.inksetter.twist.expression.StringLiteral;
import com.inksetter.twist.expression.TernaryExpression;
import com.inksetter.twist.expression.operators.AndExpression;
import com.inksetter.twist.expression.operators.NotExpression;
import com.inksetter.twist.expression.operators.OrExpression;
import com.inksetter.twist.expression.operators.arith.DivisionExpression;
import com.inksetter.twist.expression.operators.arith.MinusExpression;
import com.inksetter.twist.expression.operators.arith.ModExpression;
import com.inksetter.twist.expression.operators.arith.MultiplyExpression;
import com.inksetter.twist.expression.operators.arith.PlusExpression;
import com.inksetter.twist.expression.operators.compare.EqualsExpression;
import com.inksetter.twist.expression.operators.compare.GreaterThanExpression;
import com.inksetter.twist.expression.operators.compare.GreaterThanOrEqualsExpression;
import com.inksetter.twist.expression.operators.compare.LessThanExpression;
import com.inksetter.twist.expression.operators.compare.LessThanOrEqualsExpression;
import com.inksetter.twist.expression.operators.compare.LikeExpression;
import com.inksetter.twist.expression.operators.compare.NotEqualsExpression;
import com.inksetter.twist.expression.operators.compare.NotLikeExpression;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The core command parser for Twist command syntax.  This parser reads string input
 * and produces an executable CommandSequence tree.
 */
public class TwistParser {

    public TwistParser(CharSequence script) {
        _scan = new TwistLexer(script);
    }
    
    public StatementSequence parse() throws TwistParseException {
        _scan.next();
        
        StatementSequence seq = buildSequence();
        
        if (_scan.tokenType() != TwistTokenType.END) {
            throw new TwistParseException(_scan.getLine() + 1, _scan.getLinePos() + 1, "Unexpected token: " + _scan.current());
        }

        return seq;
    }
    
    //
    // Implementation
    //

    protected StatementSequence buildSequence() throws TwistParseException {
        StatementSequence sequence = new StatementSequence();
        
        ExecutableStatement statement = buildStatement();
        sequence.addStatement(statement);
        
        while (_scan.tokenType() == TwistTokenType.SEMICOLON) {
            // Skip the semicolon
            _scan.next();

            // Special case -- if someone ends a command sequence with a semicolon, check for reasonable
            // end-of-sequence characters.
            if (_scan.tokenType() == TwistTokenType.CLOSE_BRACE || _scan.tokenType() == TwistTokenType.END) {
                break;
            }

            statement = buildStatement();
            sequence.addStatement(statement);
        }
        
        return sequence;
    }
    
    protected Expression buildIfExpression() throws TwistParseException {
        _scan.next();
        if (_scan.tokenType() != TwistTokenType.OPEN_PAREN) {
            throw parseException("(");
        }
        
        _scan.next();
        return buildFullExpression();
    }
    
    protected ExecutableStatement buildStatement() throws TwistParseException {
        ExecutableStatement stmt = new ExecutableStatement();

        if (_scan.tokenType() == TwistTokenType.IF) {
            stmt.setIfTest(buildIfExpression());

            if (_scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();

            stmt.setIfBlock(buildStatement());

            if (_scan.tokenType() == TwistTokenType.ELSE) {
                _scan.next();
                stmt.setElseBlock(buildStatement());
            }
        }
        else if (_scan.tokenType() == TwistTokenType.TRY) {
            _scan.next();

            // We expect braces here, but we want this to be
            if (_scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException("{");
            }

            // Don't scan the brace.  Let the main block parser do that.
            stmt.setSubSequence(buildSubSequence());
            stmt.setCatchBlocks(buildCatchBlocks());
            if (_scan.tokenType() == TwistTokenType.FINALLY) {
                // Scan past the FINALLY keyword
                _scan.next();
                stmt.setFinallyBlock(buildSubSequence());
            }
        }
        else {
            if (_scan.tokenType() == TwistTokenType.OPEN_BRACE) {
                stmt.setSubSequence(buildSubSequence());
            }
            else {
                if (_scan.tokenType() == TwistTokenType.IDENTIFIER) {
                    String identifier = getIdentifier("NAME");
                    TwistLexer.TwistToken save = _scan.current();
                    _scan.next();

                    if (_scan.tokenType() == TwistTokenType.ASSIGNMENT) {
                        // Assignment
                        _scan.next();
                        stmt.setAssignment(identifier);
                    }
                    else {
                        _scan.reset(save);
                    }
                }
                stmt.setExpression(buildFullExpression());
            }
        }

        return stmt;
    }
    
    protected List<CatchBlock> buildCatchBlocks() throws TwistParseException {
        if (_scan.tokenType() != TwistTokenType.CATCH) {
            return null;
        }

        List<CatchBlock> blocks = new ArrayList<>();
        
        while (_scan.tokenType() == TwistTokenType.CATCH) {
            CatchBlock block = new CatchBlock();
            _scan.next();
            if (_scan.tokenType() != TwistTokenType.OPEN_PAREN) {
                throw parseException("(");
            }
            _scan.next();

            block.setType(getIdentifier("TypeName"));
            _scan.next();

            block.setVarName(getIdentifier("NAME"));
            _scan.next();

            if (_scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();
            
            if (_scan.tokenType() != TwistTokenType.OPEN_BRACE) {
                throw parseException("{");
            }
            
            _scan.next();
            
            // If it is not a close brace then assume it is a sequence
            // If it was a close brace we just leave the sequence empty
            // as in ignoring the exception
            if (_scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                block.setBlock(buildSequence());    
            }
            
            if (_scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
                throw parseException("}");
            }

            _scan.next();
            
            blocks.add(block);
        }
        return blocks;
    }
    
    protected StatementSequence buildSubSequence() throws TwistParseException {
        if (_scan.tokenType() != TwistTokenType.OPEN_BRACE) {
            throw parseException("{");
        }

        _scan.next();
        StatementSequence seq = buildSequence(); 
        
        if (_scan.tokenType() != TwistTokenType.CLOSE_BRACE) {
            throw parseException("}");
        }
        _scan.next();
        
        return seq;
    }

    protected Expression buildExpressionTerm() throws TwistParseException {
        Expression expr = buildExpressionFactor();
        do {
            TwistTokenType operatorToken = _scan.tokenType();
            if (operatorToken == TwistTokenType.PLUS) {
                _scan.next();
                expr = new PlusExpression(expr, buildExpressionFactor());
            }
            else if (operatorToken == TwistTokenType.MINUS) {
                _scan.next();
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
        TwistTokenType oper = _scan.tokenType();
        if (oper == TwistTokenType.EQ) {
            _scan.next();
            expr  = new EqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NE) {
            _scan.next();
            expr  = new NotEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.GT) {
            _scan.next();
            expr  = new GreaterThanExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.GE) {
            _scan.next();
            expr  = new GreaterThanOrEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LT) {
            _scan.next();
            expr  = new LessThanExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LE) {
            _scan.next();
            expr  = new LessThanOrEqualsExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.LIKE) {
            _scan.next();
            expr  = new LikeExpression(expr, buildExpressionTerm());
        }
        else if (oper == TwistTokenType.NOT) {
            _scan.next();
            if (_scan.tokenType() != TwistTokenType.LIKE) {
                throw parseException("LIKE");
            }
            _scan.next();
            expr  = new NotLikeExpression(expr, buildExpressionTerm());
        }

        // If no operator is present, just return the initial expression
        return expr;
    }
    
    protected Expression buildAndExpression() throws TwistParseException {
        Expression expr = buildLogicalExpression();
        while (_scan.tokenType() == TwistTokenType.AND) {
            _scan.next();
            Expression left = expr;
            Expression right = buildLogicalExpression();
            expr = new AndExpression(left, right);
        }
        
        return expr;
    }
    protected Expression buildFullExpression() throws TwistParseException {
        Expression expr = buildOrExpression();
        if (_scan.tokenType() == TwistTokenType.QUESTION) {
            _scan.next();
            Expression ternaryIf = expr;
            Expression ternaryThen = buildFullExpression();
            if (_scan.tokenType() != TwistTokenType.COLON) {
                throw parseException(":");
            }
            _scan.next();
            Expression ternaryElse = buildFullExpression();
            expr = new TernaryExpression(ternaryIf, ternaryThen, ternaryElse);
        }

        return expr;
    }

    protected Expression buildOrExpression() throws TwistParseException {
        Expression expr = buildAndExpression();
        while (_scan.tokenType() == TwistTokenType.OR) {
            _scan.next();
            Expression left = expr;
            Expression right = buildAndExpression();
            expr = new OrExpression(left, right);
        }
        
        return expr;
    }
    
    protected Expression buildExpressionFactor() throws TwistParseException {
        Expression expr = buildExpressionValue();
        
        do {
            TwistTokenType operatorToken = _scan.tokenType();
            
            if (operatorToken == TwistTokenType.STAR) {
                _scan.next();
                expr = new MultiplyExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == TwistTokenType.SLASH) {
                _scan.next();
                expr = new DivisionExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == TwistTokenType.PERCENT) {
                _scan.next();
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
        while (_scan.tokenType() == TwistTokenType.DOT || _scan.tokenType() == TwistTokenType.OPEN_BRACKET) {
            if (_scan.tokenType() == TwistTokenType.DOT) {
                _scan.next();
                if (_scan.tokenType() != TwistTokenType.IDENTIFIER) {
                    throw parseException("identifier");
                }
                String identifier = _scan.current().getValue();
                _scan.next();
                if (_scan.tokenType() == TwistTokenType.OPEN_PAREN) {
                    List<Expression> methodArgs = getFunctionArgs();
                    expr = new MethodCallExpression(expr, identifier, methodArgs);
                }
                else {
                    expr = new MemberExpression(expr, identifier);
                }
            }
            else {
                _scan.next();
                Expression element = buildFullExpression();
                if (_scan.tokenType() != TwistTokenType.CLOSE_BRACKET) {
                    throw parseException("]");
                }
                _scan.next();
                expr = new ElementExpression(expr, element);
            }
        }

        return expr;
    }
    protected Expression buildExpressionPossibleValue() throws TwistParseException {
        boolean isNegative = false;
        switch (_scan.tokenType()) {
        case BANG:
            _scan.next();
            return new NotExpression(buildExpressionValue());
        case IDENTIFIER:
            String identifier = _scan.current().getValue();
            _scan.next();
            if (_scan.tokenType() == TwistTokenType.OPEN_PAREN) {
                return buildFunctionExpression(identifier);
            }
            else {
                return new ReferenceExpression(identifier);
            }
        case OPEN_PAREN:
            _scan.next();
            Expression subExpression = buildFullExpression();
            if (_scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();
            return subExpression;
        case MINUS:
            isNegative = true;
            // Pass through
        case PLUS:
            _scan.next();
            if (_scan.tokenType() != TwistTokenType.NUMBER) {
                throw parseException("<NUMBER>");
            }
            // Pass through
        case NUMBER:
            String numericValue = _scan.current().getValue();
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
            
            _scan.next();
            
            return numericExpression;

        case SINGLE_STRING:
        case DOUBLE_STRING:
            String unquoted = _scan.current().getValue();
            _scan.next();
            return new StringLiteral(dequote(unquoted));
            
        case NULL_TOKEN:
            _scan.next();
            return new LiteralExpression(TwistDataType.STRING, null);

        case TRUE:
            _scan.next();
            return new LiteralExpression(TwistDataType.BOOLEAN, Boolean.TRUE);

        case FALSE:
            _scan.next();
            return new LiteralExpression(TwistDataType.BOOLEAN, Boolean.FALSE);
        }

        // Now, we've failed to find an expression.
        throw parseException("expression");
    }
    
    protected Expression buildFunctionExpression(String functionName) throws TwistParseException {
        // This method only gets called if parentheses have been seen
        List<Expression> functionArgs = getFunctionArgs();

        return FunctionExpression.chooseFunction(functionName, functionArgs);
    }

    private List<Expression> getFunctionArgs() throws TwistParseException {
        List<Expression> functionArgs = new ArrayList<>();

        if (_scan.tokenType() == TwistTokenType.OPEN_PAREN) {
            _scan.next();
            while (_scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                functionArgs.add(buildFullExpression());
                if (_scan.tokenType() == TwistTokenType.COMMA) {
                    _scan.next();
                }
                else if (_scan.tokenType() != TwistTokenType.CLOSE_PAREN) {
                    throw parseException(")");
                }
            }
            _scan.next();
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
        if (_scan.tokenType() == TwistTokenType.IDENTIFIER) {
            return _scan.current().getValue();
        }

        throw parseException(expect);
    }
    
    protected TwistParseException parseException(String expected) {
        return new TwistParseException(_scan.getLine() + 1, _scan.getLinePos() + 1,
                "Expected: " + expected + ", got " + _scan.current());
    }

    protected final TwistLexer _scan;
}
