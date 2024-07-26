package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class GreaterThanOrEqualsExpression extends AbsractOperExpression {
    public GreaterThanOrEqualsExpression(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected Boolean doOper(Object left, Object right) {
        return compare(left, right);
    }

    private boolean compare(Object left, Object right) {
        return ValueUtils.compare(left, right) >= 0;
    }


    @Override
    protected String operString() {
        return ">=";
    }
}
