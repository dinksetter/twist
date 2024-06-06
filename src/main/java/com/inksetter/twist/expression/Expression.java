package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.EvalContext;

public interface Expression {
    Object evaluate(EvalContext ctx) throws TwistException;
}
