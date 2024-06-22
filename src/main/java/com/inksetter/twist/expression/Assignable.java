package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ScriptContext;

public interface Assignable {
    Object assignValue(ScriptContext exec, Object value) throws TwistException;
}
