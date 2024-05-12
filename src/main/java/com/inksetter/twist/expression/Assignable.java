package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public interface Assignable {
    Object assignValue(ExecContext exec, Object value) throws TwistException;
}
