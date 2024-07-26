package com.inksetter.twist;

public interface Expression {
    Object evaluate(EvalContext ctx) throws TwistException;
}
