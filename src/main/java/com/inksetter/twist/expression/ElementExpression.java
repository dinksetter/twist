package com.inksetter.twist.expression;

import com.inksetter.twist.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class ElementExpression implements Assignable {

    public ElementExpression(Expression target, Expression element) {
        _element = element;
        _target = target;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object value = _target.evaluate(ctx);
        if (value == null) {
            throw new NullValueException(_target + " is null");
        }

        Object indexVal = _element.evaluate(ctx);
        if (indexVal == null) {
            throw new NullValueException(_element + " is null");
        }

        // Maps are indexed by key, everything else by position.
        if (value instanceof Map<?,?>) {
            return ((Map<?,?>) value).get(indexVal);
        }

        if (value instanceof List<?>) {
            return ((List<?>)value).get(ValueUtils.asInt(indexVal));
        }

        if (value.getClass().isArray()) {
            return Array.get(value, ValueUtils.asInt(indexVal));
        }

        throw new TypeMismatchException("Expected array, list or map, got " + value);
    }

    @Override
    public void assignValue(EvalContext exec, Object value) throws TwistException {
        Object obj = _target.evaluate(exec);

        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        Object elementObj = _element.evaluate(exec);
        if (obj instanceof Map) {
            ((Map<Object, Object>) obj).put(elementObj, value);
        }
        else if (obj.getClass().isArray()) {
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
            throw new TypeMismatchException("Expected array, list or map type");
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
