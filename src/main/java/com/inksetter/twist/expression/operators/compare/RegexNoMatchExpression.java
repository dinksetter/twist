package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.Expression;

public class RegexNoMatchExpression extends LikeExpression {
    public RegexNoMatchExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Boolean doOper(Object left, Object right) {
        return !super.doOper(left, right);
    }
    
    @Override
    protected String operString() {
        return " =~ ";
    }
}
