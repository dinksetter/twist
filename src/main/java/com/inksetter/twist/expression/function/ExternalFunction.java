package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.EvalContext;
import com.inksetter.twist.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes a function using the external function feature of the execution
 * context.
 */
public class ExternalFunction implements TwistFunction {

    public ExternalFunction(String name) {
        _functionName = name;
    }

    @Override
    public Object evaluate(EvalContext ctx, List<Expression> args) throws TwistException {
        if (!ctx.functionExists(_functionName)) {
            throw new TwistException("Unrecognized Function: " + _functionName);
        }

        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return ctx.callFunction(_functionName, argValues);
    }

    private final String _functionName;
}
