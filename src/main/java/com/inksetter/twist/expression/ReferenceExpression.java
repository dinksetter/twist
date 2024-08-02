package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;

public class ReferenceExpression implements Assignable {
    public ReferenceExpression(String name) {
        _name = name;
    }

    @Override
    public Object evaluate(EvalContext ctx) {
        return ctx.getVariable(_name);
    }

    @Override
    public void assignValue(EvalContext exec, Object value) throws TwistException {
        exec.setVariable(_name, value);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _name;
    }

    private final String _name;

}
