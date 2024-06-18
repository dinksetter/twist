package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArrayExpression implements Expression {
    private final List<Expression> exprs;
    public ArrayExpression(List<Expression> exprs) {
        this.exprs = exprs;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        List<Object> data = new ArrayList<>();
        for (Expression expr : exprs) {
            data.add(expr.evaluate(ctx));
        }
        return data;
    }
}
