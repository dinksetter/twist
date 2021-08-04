package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public class DoubleLiteral implements Expression {
    public DoubleLiteral(Double value) {
        _value = value;
    }

    public Double evaluate(ExecContext ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final Double _value;
}
