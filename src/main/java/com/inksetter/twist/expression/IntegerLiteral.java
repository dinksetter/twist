package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;

import java.math.BigInteger;

public class IntegerLiteral implements Expression {
    public IntegerLiteral(BigInteger value) {
        _value = value;
    }

    public BigInteger evaluate(EvalContext ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final BigInteger _value;
}
