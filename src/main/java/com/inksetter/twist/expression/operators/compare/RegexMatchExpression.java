package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class RegexMatchExpression extends AbsractOperExpression {
    public RegexMatchExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Boolean doOper(Object left, Object right) {
        String leftValue = String.valueOf(left);
        String rightValue = String.valueOf(right);

        return leftValue.matches(rightValue);
    }
    
    @Override
    protected String operString() {
        return " =~ ";
    }
}
