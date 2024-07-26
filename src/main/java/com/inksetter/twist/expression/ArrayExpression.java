package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

import java.util.ArrayList;
import java.util.List;

public class ArrayExpression implements Expression {
    private final List<Expression> exprs;
    public ArrayExpression(List<Expression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        List<Object> data = new ArrayList<>();
        for (Expression expr : exprs) {
            data.add(expr.evaluate(ctx));
        }
        return data;
    }
}
