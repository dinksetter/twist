package com.inksetter.twist.expression;

import com.inksetter.twist.exec.SymbolSource;

public class StringLiteral implements Expression {
    public StringLiteral(String value) {
        _value = value;
    }

    public String evaluate(SymbolSource ctx) {
        return _value;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "'" + _value + "'";
    }

    private final String _value;
}
