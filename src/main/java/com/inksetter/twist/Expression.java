package com.inksetter.twist;

public interface Expression {
    Object evaluate(EvalContext ctx) throws TwistException;

    default <T> T evaluate(EvalContext ctx, Class<T> cls) throws TwistException {
        return ValueUtils.asType(evaluate(ctx), cls);
    }
}
