package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;

public abstract class AbsractOperExpression implements Expression {
    public AbsractOperExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object leftValue = _left.evaluate(ctx);
        Object rightValue = _right.evaluate(ctx);
        
        return doOper(leftValue, rightValue);
    }
    
    @Override
    public String toString() {
    	return _left.toString() + operString() + _right.toString();
    }
    
    protected String operString() {
    	return "";
    }
    
    protected abstract Object doOper(Object left, Object right) throws TwistException;

    Expression getLeft() { return _left; }

    Expression getRight() { return _right; }

    private final Expression _left;
    private final Expression _right;
}
