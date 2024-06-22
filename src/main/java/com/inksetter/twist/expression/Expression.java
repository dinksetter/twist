package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;

public interface Expression {
    Object evaluate(EvalContext ctx) throws TwistException;
}
