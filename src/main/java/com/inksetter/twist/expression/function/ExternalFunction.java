package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes a function using the external function feature of the execution
 * context.
 */
public final class ExternalFunction implements TwistFunction {
    private final GenericFunction _call;
    public ExternalFunction(GenericFunction call) {
        _call = call;
    }

    @Override
    public Object evaluate(SymbolSource ctx, List<Expression> args) throws TwistException {
        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return _call.invoke(argValues);
    }

}
