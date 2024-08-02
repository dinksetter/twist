package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

public interface Assignable extends Expression {
    void assignValue(EvalContext exec, Object value) throws TwistException;
}
