package com.inksetter.twist.expression;

import com.inksetter.twist.exec.SymbolSource;

public class IntegerLiteral implements Expression {
    public IntegerLiteral(Integer value) {
        _value = value;
    }

    public Integer evaluate(SymbolSource ctx) {
        return _value;
    }

    @Override
    public String toString() { return _value.toString(); }

    private final Integer _value;
}
