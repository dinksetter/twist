package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;

public class NotEqualsExpression extends EqualsExpression {
    public NotEqualsExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    @Override
    protected boolean compare(TwistValue left, TwistValue right) {
        return !super.compare(left, right);
    }
    
    @Override
    protected String operString() {
        return "!=";
    }
}
