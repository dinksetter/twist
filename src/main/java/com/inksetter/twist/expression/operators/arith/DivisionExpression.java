package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class DivisionExpression extends AbsractOperExpression {
    public DivisionExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Object doOper(Object left, Object right)
            throws DivideByZeroException {
        if (left instanceof Integer && right instanceof Integer) {
            int leftNum = ValueUtils.asInt(left);
            int rightNum = ValueUtils.asInt(right);

            if (rightNum == 0) {
                throw new DivideByZeroException();
            }

            return leftNum / rightNum;
        }
        else {
            double leftNum = ValueUtils.asDouble(left);
            double rightNum = ValueUtils.asDouble(right);

            if (rightNum == 0.0) {
                throw new DivideByZeroException();
            }

            return leftNum / rightNum;
        }
    }
    
    @Override
    protected String operString() {
        return " / ";
    }
}
