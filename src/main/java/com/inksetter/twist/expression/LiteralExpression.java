package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistDataType;

public class LiteralExpression implements Expression {
    public LiteralExpression(Object value) {
        this(TwistDataType.lookupClass(value.getClass()), value);
    }
    
    public LiteralExpression(TwistDataType type, Object value) {
        _type = type;
        _value = value;
    }
    
    public Object evaluate(EvalContext ctx) {
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
