package com.inksetter.twist.expression;

import com.inksetter.twist.*;

import java.lang.reflect.Array;
import java.util.List;

public class ElementExpression implements Assignable {

    public ElementExpression(Expression target, Expression element) {
        _element = element;
        _target = target;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
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

    @Override
    public void assignValue(EvalContext exec, Object value) throws TwistException {
        Object obj = _target.evaluate(exec);

        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        Object elementObj = _element.evaluate(exec);
        if (obj.getClass().isArray()) {
            if (!(elementObj instanceof Number)) {
                throw new TypeMismatchException("Expected number");
            }
            Array.set(obj, ((Number) elementObj).intValue(), value);
        }
        else if (obj instanceof List) {
            if (!(elementObj instanceof Number)) {
                throw new TypeMismatchException("Expected number");
            }
            ((List) obj).set(((Number) elementObj).intValue(), value);
        }
        else {
            throw new TypeMismatchException("Expected array or list type");
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "[" + _element + "]";
    }

    private final Expression _target;
    private final Expression _element;
}
