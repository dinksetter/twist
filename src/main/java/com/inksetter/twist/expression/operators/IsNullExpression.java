package com.inksetter.twist.expression.operators;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

public class IsNullExpression implements Expression {
    public IsNullExpression(Expression target) {
        _target = target;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue targetValue = _target.evaluate(ctx);
        return new TwistValue(TwistDataType.BOOLEAN, targetValue.isNull());
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "" + _target + " IS NULL";
    }

    
    private final Expression _target;
}
