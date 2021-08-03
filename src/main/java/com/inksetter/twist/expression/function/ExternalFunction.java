package com.inksetter.twist.expression.function;

import java.util.ArrayList;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.exec.ExecContext;

/**
 * Executes a function using the external function feature of the execution
 * context.
 */
public class ExternalFunction implements TwistFunction {

    public ExternalFunction(String name) {
        _functionName = name;
    }

    @Override
    public Object evaluate(ExecContext ctx, List<Expression> args) throws TwistException {
        if (!ctx.lookupExternalFunction(_functionName)) {
            throw new TwistException("Unrecognized Function: " + _functionName);
        }

        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return ctx.invokeExternalFunction(_functionName, argValues);
    }

    private final String _functionName;
}
