package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class GreaterThanOrEqualsExpression extends AbsractOperExpression {
    public GreaterThanOrEqualsExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        return new TwistValue(TwistDataType.BOOLEAN, compare(left, right));
    }
    
    private boolean compare(TwistValue left, TwistValue right) {
        return (left.compareTo(right) >= 0);
    }
    
    @Override
    protected String operString() {
        return ">=";
    }
}
