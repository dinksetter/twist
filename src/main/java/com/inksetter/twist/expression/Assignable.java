package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.EvalContext;

public interface Assignable {
    Object assignValue(EvalContext exec, Object value) throws TwistException;
}
