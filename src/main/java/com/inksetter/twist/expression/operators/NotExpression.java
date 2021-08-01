package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

public class NotExpression implements Expression {
    public NotExpression(Expression target) {
        _target = target;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue targetValue = _target.evaluate(ctx);
        
        boolean result = !targetValue.asBoolean();

        return new TwistValue(TwistDataType.BOOLEAN, result);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "!" + _target;
    }

    
    private final Expression _target;
}

