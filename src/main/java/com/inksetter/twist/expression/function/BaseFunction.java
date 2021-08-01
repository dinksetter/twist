package com.inksetter.twist.expression.function;

import java.util.ArrayList;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

/**
 * An abstract base class that evaluates all argument expressions before calling the invoke method.
 */
public abstract class BaseFunction implements TwistFunction {

    @Override
    public TwistValue evaluate(ExecContext ctx, List<Expression> args) throws TwistException {
        List<TwistValue> argValues = new ArrayList<>();
        
        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }
        
        return invoke(ctx, argValues);
    }
    
    /**
     * Invokes the function implementation with all arguments evaluated.
     * @param ctx The <code>ServerContext</code> to use for execution of the
     *            function, if it needs to get data or execute commands within
     *            the server execution context.
     * @param argValues Pre-evaluated arguments to the function call.
     * @return a single value.
     * @throws TwistException if an error occurred during function execution.
     */
    protected abstract TwistValue invoke(ExecContext ctx, List<TwistValue> argValues) throws TwistException;
}
