package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.expression.Expression;

public class OrExpression implements Expression {
    public OrExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public Object evaluate(SymbolSource ctx) throws TwistException {
        Object leftValue = _left.evaluate(ctx);
        boolean result;

        if (ValueUtils.asBoolean(leftValue)) {
            result = true;
        }
        else {
            Object rightValue = _right.evaluate(ctx);
            result = ValueUtils.asBoolean(rightValue);
        }

        return result;
    }
    
    @Override
    public String toString() {
        return _left.toString() + " || " + _right.toString();
    }
    
    private final Expression _left;
    private final Expression _right;    
}
