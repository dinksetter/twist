package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class that evaluates all argument expressions before calling the invoke method.
 */
public abstract class BaseFunction implements TwistFunction {

    @Override
    public Object evaluate(SymbolSource ctx, List<Expression> args) throws TwistException {
        validateArgs(args);

        List<Object> argValues = new ArrayList<>(args.size());

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
    protected abstract Object invoke(SymbolSource ctx, List<Object> argValues) throws TwistException;

    protected void validateArgs(List<Expression> args) throws TwistException {
        // do nothing.
    }
}
