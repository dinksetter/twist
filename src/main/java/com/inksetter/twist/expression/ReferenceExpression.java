package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;

public class ReferenceExpression implements Expression {
    public ReferenceExpression(String name) {
        _name = name;
    }
    
    public Object evaluate(EvalContext ctx) {
        return ctx.getVariable(_name);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _name;
    }

    private final String _name;

}
