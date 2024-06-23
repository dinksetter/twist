package com.inksetter.twist.expression.operators;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.Expression;

public class IfNullExpression implements Expression {
    public IfNullExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public Object evaluate(EvalContext ctx) throws TwistException {

        Object test = _left.evaluate(ctx);

        if (!ValueUtils.isNull(test)) {
            return test;
        }
        else {
            return _right.evaluate(ctx);
        }
    }
    
    @Override
    public String toString() {
        return _left.toString() + " ?: " + _right.toString();
    }
    
    private final Expression _left;
    private final Expression _right;
}
