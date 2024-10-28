package com.inksetter.twist.expression;

import com.inksetter.twist.*;

public class IncrementExpression implements Expression {
    private final Assignable target;
    private final boolean increment;

    public IncrementExpression(Assignable target, boolean increment) {
        this.target = target;
        this.increment = increment;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object value = target.evaluate(ctx);
        TwistDataType type = ValueUtils.getType(value);
        switch (type) {
            case INTEGER:
                target.assignValue(ctx, ValueUtils.asInt(value) + (increment ? 1 : -1));
                break;
            case LONG:
                target.assignValue(ctx, ValueUtils.asLong(value) + (increment ? 1L : -1L));
                break;
            default:
                throw new TypeMismatchException("expected number");
        }
        return value;
    }
}
