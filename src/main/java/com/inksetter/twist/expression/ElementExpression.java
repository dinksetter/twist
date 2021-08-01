package com.inksetter.twist.expression;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

import java.util.List;

public class ElementExpression implements Expression {

    public ElementExpression(Expression target, Expression element) {
        _element = element;
        _target = target;
    }

    @Override
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue value = _target.evaluate(ctx);
        TwistValue index = _element.evaluate(ctx);
        if (value.getType() != TwistDataType.ARRAY) {
            throw new TypeMismatchException("Expected array, got " + value.getType());
        }

        Object actualValue = value.getValue();
        if (actualValue == null) {
            throw new NullValueException(_target + " is null");
        }

        Class<?> actualClass = actualValue.getClass();
        Object result = null;
        if (actualValue instanceof List<?>) {
            result = ((List<?>)actualValue).get(index.asInt());
        }
        else if (actualClass.isArray()) {
            result = ((Object[])actualValue)[index.asInt()];
        }

        return new TwistValue(result);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "[" + _element + "]";
    }

    private final Expression _target;
    private final Expression _element;
}
