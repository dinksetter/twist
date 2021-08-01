package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class ModExpression extends AbsractOperExpression {
    public ModExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right)
            throws DivideByZeroException {
        int leftNum = left.asInt();
        int rightNum = right.asInt();

        if (rightNum == 0) {
            throw new DivideByZeroException();
        }

        return new TwistValue(TwistDataType.INTEGER, leftNum % rightNum);
    }
    
    @Override
    protected String operString() {
        return " % ";
    }

}
