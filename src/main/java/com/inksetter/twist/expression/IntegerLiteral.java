package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public class IntegerLiteral implements Expression {
    public IntegerLiteral(Integer value) {
        _value = value;
    }

    public Integer evaluate(ExecContext ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final Integer _value;
}
