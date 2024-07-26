package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class MultiplyExpression extends AbsractOperExpression {
    public MultiplyExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Object doOper(Object left, Object right) {
        if (left instanceof Double || right instanceof Double) {
            return ValueUtils.asDouble(left) * ValueUtils.asDouble(right);
        }
        else {
            return ValueUtils.asInt(left) * ValueUtils.asInt(right);
        }
    }
    
    @Override
    protected String operString() {
        return " * ";
    }

}
