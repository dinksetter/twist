package com.inksetter.twist.expression;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;

import java.util.List;

public class ElementExpression implements Expression {

    public ElementExpression(Expression target, Expression element) {
        _element = element;
        _target = target;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        Object value = _target.evaluate(ctx);
        if (ValueUtils.getType(value) != TwistDataType.ARRAY) {
            throw new TypeMismatchException("Expected array, got " + value);
        }

        if (value == null) {
            throw new NullValueException(_target + " is null");
        }
        Object indexVal = _element.evaluate(ctx);
        if (indexVal == null) {
            throw new NullValueException(_element + " is null");
        }

        int index = ValueUtils.asInt(indexVal);

        Class<?> valueClass = value.getClass();
        Object result = null;
        if (value instanceof List<?>) {
            result = ((List<?>)value).get(index);
        }
        else if (valueClass.isArray()) {
            result = ((Object[])value)[index];
        }

        return result;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "[" + _element + "]";
    }

    private final Expression _target;
    private final Expression _element;
}
