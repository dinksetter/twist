package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

public class AssignmentExpression implements Expression {
    private final Assignable left;
    private final Expression right;

    public AssignmentExpression(Assignable left, Expression right) {
        this.left = left;
        this.right = right;
    }


    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object value = right.evaluate(ctx);
        left.assignValue(ctx, value);
        return value;
    }
}
