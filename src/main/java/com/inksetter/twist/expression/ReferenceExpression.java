package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;

public class ReferenceExpression implements Assignable {
    public ReferenceExpression(String name) {
        this.name = name;
    }

    @Override
    public Object evaluate(EvalContext ctx) {
        return ctx.getVariable(name);
    }

    @Override
    public void assignValue(EvalContext exec, Object value) throws TwistException {
        exec.setVariable(name, value);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    private final String name;
}
