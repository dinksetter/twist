package com.inksetter.twist.expression;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public class LiteralExpression implements Expression {
    public LiteralExpression(Object value) {
        this(TwistDataType.lookupClass(value.getClass()), value);
    }
    
    public LiteralExpression(TwistDataType type, Object value) {
        _type = type;
        _value = value;
    }
    
    public Object evaluate(ExecContext ctx) {
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
