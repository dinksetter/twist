package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.Expression;

public class NotEqualsExpression extends EqualsExpression {
    public NotEqualsExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    @Override
    protected boolean compare(Object left, Object right) {
        return !super.compare(left, right);
    }
    
    @Override
    protected String operString() {
        return "!=";
    }
}
