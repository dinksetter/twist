package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.TypeMismatchException;

/**
 * Unary minus, as in <code>-x</code>.
 */
public class NegateExpression implements Expression {
    private final Expression target;

    public NegateExpression(Expression target) {
        this.target = target;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object value = target.evaluate(ctx);

        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return -((Double) value);
        }
        if (value instanceof Long) {
            return -((Long) value);
        }
        if (value instanceof Integer) {
            return -((Integer) value);
        }
        if (value instanceof Number) {
            return -((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.indexOf('.') != -1 || str.indexOf('e') != -1 || str.indexOf('E') != -1) {
                return -ValueUtils.asDouble(str);
            }
            return -ValueUtils.asInt(str);
        }

        throw new TypeMismatchException("cannot negate " + value);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "-" + target;
    }
}
