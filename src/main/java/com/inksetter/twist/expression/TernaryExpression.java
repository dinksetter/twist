package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * An expression that represents a ternary operator (test ? then : else)
 */
public class TernaryExpression implements Expression {

    public TernaryExpression(Expression testExpr, Expression thenExpr, Expression elseExpr) {
        _testExpr = testExpr;
        _thenExpr = thenExpr;
        _elseExpr = elseExpr;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue leftValue = _testExpr.evaluate(ctx);

        if (leftValue.asBoolean()) {
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
