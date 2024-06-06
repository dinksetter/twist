package com.inksetter.twist.expression;

import com.inksetter.twist.exec.EvalContext;

public class DoubleLiteral implements Expression {
    public DoubleLiteral(Double value) {
        _value = value;
    }

    public Double evaluate(EvalContext ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final Double _value;
}
