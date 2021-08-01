package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class MultiplyExpression extends AbsractOperExpression {
    public MultiplyExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        if (left.getType() == TwistDataType.DOUBLE || right.getType() == TwistDataType.DOUBLE) {
            return new TwistValue(TwistDataType.DOUBLE, left.asDouble() * right.asDouble());
        }
        else {
            return new TwistValue(TwistDataType.INTEGER, left.asInt() * right.asInt());
        }
    }
    
    @Override
    protected String operString() {
        return " * ";
    }

}
