package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

public abstract class AbsractOperExpression implements Expression {
    public AbsractOperExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue leftValue = _left.evaluate(ctx);
        TwistValue rightValue = _right.evaluate(ctx);
        
        return doOper(leftValue, rightValue);
    }
    
    @Override
    public String toString() {
    	return _left.toString() + operString() + _right.toString();
    }
    
    protected String operString() {
    	return "";
    }
    
    protected abstract TwistValue doOper(TwistValue left, TwistValue right)
        throws TwistException;
    
    private final Expression _left;
    private final Expression _right;
}
