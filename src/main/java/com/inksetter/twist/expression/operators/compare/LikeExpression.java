package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class LikeExpression extends AbsractOperExpression {
    public LikeExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Boolean doOper(Object left, Object right) {
        String leftValue = String.valueOf(left);
        String rightValue = String.valueOf(right);
        
        return new LikeMatcher(rightValue).match(leftValue);
    }
    
    @Override
    protected String operString() {
        return " LIKE ";
    }
}
