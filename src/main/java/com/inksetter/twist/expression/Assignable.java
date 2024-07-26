package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ScriptContext;

public interface Assignable {
    Object assignValue(ScriptContext exec, Object value) throws TwistException;
}
