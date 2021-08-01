package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class NotLikeExpression extends AbsractOperExpression {
    public NotLikeExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        String leftValue = String.valueOf(left.getValue());
        String rightValue = String.valueOf(right.getValue());
        
        boolean likeMatches = new LikeMatcher(rightValue).match(leftValue);
        
        return new TwistValue(TwistDataType.BOOLEAN, !likeMatches);
    }

    @Override
    protected String operString() {
        return " NOT LIKE ";
    }
}
