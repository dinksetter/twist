package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ScriptContext;

public class IdentifierAssignable implements Assignable {
    private final String name;
    public IdentifierAssignable(String name) {
        this.name = name;
    }

    @Override
    public Object assignValue(ScriptContext exec, Object value) throws TwistException {
        exec.setVariable(name, value);
        return value;
    }
}
