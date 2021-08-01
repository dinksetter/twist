package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class DivisionExpression extends AbsractOperExpression {
    public DivisionExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right)
            throws DivideByZeroException {
        double leftNum = left.asDouble();
        double rightNum = right.asDouble();
        
        if (rightNum == 0.0) {
            throw new DivideByZeroException();
        }
        
        return new TwistValue(TwistDataType.DOUBLE, leftNum / rightNum);
    }
    
    @Override
    protected String operString() {
        return " / ";
    }

}
