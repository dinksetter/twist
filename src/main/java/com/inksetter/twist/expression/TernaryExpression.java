package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

/**
 * An expression that represents a ternary operator (test ? then : else)
 */
public class TernaryExpression implements Expression {

    public TernaryExpression(Expression testExpr, Expression thenExpr, Expression elseExpr) {
        _testExpr = testExpr;
        _thenExpr = thenExpr;
        _elseExpr = elseExpr;
    }
    
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object testValue = _testExpr.evaluate(ctx);

        if (ValueUtils.asBoolean(testValue)) {
            return _thenExpr.evaluate(ctx);
        }
        else {
            return _elseExpr.evaluate(ctx);
        }
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _testExpr.toString() + " : " + _thenExpr + " ? " + _elseExpr;
    }

    private final Expression _testExpr;
    private final Expression _thenExpr;
    private final Expression _elseExpr;
}
