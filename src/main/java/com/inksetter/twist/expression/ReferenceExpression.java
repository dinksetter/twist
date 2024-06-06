package com.inksetter.twist.expression;

import com.inksetter.twist.exec.EvalContext;

public class ReferenceExpression implements Expression {
    public ReferenceExpression(String name) {
        _name = name;
    }
    
    public Object evaluate(EvalContext ctx) {
        return ctx.lookup(_name);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _name;
    }

    private final String _name;

}
