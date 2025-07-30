package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;

import java.math.BigDecimal;

public class DoubleLiteral implements Expression {
    public DoubleLiteral(BigDecimal value) {
        _value = value;
    }

    public BigDecimal evaluate(EvalContext ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final BigDecimal _value;
}
