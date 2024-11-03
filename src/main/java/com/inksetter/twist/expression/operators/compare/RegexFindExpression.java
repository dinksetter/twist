package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

import java.util.regex.Pattern;

public class RegexFindExpression extends AbsractOperExpression {
    public RegexFindExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Boolean doOper(Object left, Object right) {
        String leftValue = String.valueOf(left);
        String rightValue = String.valueOf(right);

        Pattern regex = Pattern.compile(rightValue);
        return regex.matcher(leftValue).find();
    }
    
    @Override
    protected String operString() {
        return " =~ ";
    }
}
