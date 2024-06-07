package com.inksetter.twist.expression;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.exec.SymbolSource;

public class LiteralExpression implements Expression {
    public LiteralExpression(TwistDataType type, Object value) {
        _type = type;
        _value = value;
    }
    
    public Object evaluate(SymbolSource ctx) {
        return _value;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "(TYPE: " + _type +", VALUE: " + _value + ")";
    }

    private final TwistDataType _type;
    private final Object _value;
}
