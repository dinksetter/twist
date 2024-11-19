package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;

import java.util.List;

/**
 * An abstract base class that evaluates all argument expressions before calling the invoke method.
 */
public abstract class BaseFunction implements TwistFunction {
    /**
     * Invokes the function implementation with all arguments evaluated.
     *
     * @param argValues Pre-evaluated arguments to the function call.
     * @param context
     * @return a single value.
     * @throws TwistException if an error occurred during function execution.
     */
    public abstract Object invoke(List<Object> argValues, EvalContext context) throws TwistException;

}
