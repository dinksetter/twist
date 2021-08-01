package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

public class OrExpression implements Expression {
    public OrExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue leftValue = _left.evaluate(ctx);
        boolean result;
        
        if (leftValue.asBoolean()) {
            result = true;
        }
        else {
            TwistValue rightValue = _right.evaluate(ctx);
            result = rightValue.asBoolean();
        }
        
        return new TwistValue(TwistDataType.BOOLEAN, result);
    }
    
    @Override
    public String toString() {
        return _left.toString() + " OR " + _right.toString();
    }
    
    private final Expression _left;
    private final Expression _right;    
}
