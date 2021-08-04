package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.expression.Expression;

public class NotLikeExpression extends LikeExpression {
    public NotLikeExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Boolean doOper(Object left, Object right) {
        return !super.doOper(left,right);
    }

    @Override
    protected String operString() {
        return " NOT LIKE ";
    }
}
