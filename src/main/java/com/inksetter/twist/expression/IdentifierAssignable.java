package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public class IdentifierAssignable implements Assignable {
    private final String name;
    public IdentifierAssignable(String name) {
        this.name = name;
    }

    @Override
    public Object assignValue(ExecContext exec, Object value) throws TwistException {
        exec.setVariable(name, value);
        return value;
    }
}
