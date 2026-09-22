package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

import java.util.List;

/**
 * A function that receives its arguments as unevaluated expressions. Use this when a function needs
 * to see how an argument was written, as in <code>assertThrows(NumberFormatException, ...)</code>,
 * or to decide whether and when to evaluate one.
 * <p>
 * An implementation must evaluate each argument it uses exactly once.
 */
public interface ExpressionFunction extends TwistFunction {

    Object invokeRaw(List<Expression> args, EvalContext ctx) throws TwistException;

    /**
     * Only reached if this function is invoked as a plain function value, where the caller has
     * already evaluated the arguments and has no expressions to pass.
     */
    @Override
    default Object invoke(List<Object> args, EvalContext ctx) throws TwistException {
        throw new TwistException(getClass().getSimpleName() + " must be called directly");
    }
}
