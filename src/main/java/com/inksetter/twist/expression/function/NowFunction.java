package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.EvalContext;
import com.inksetter.twist.expression.Expression;

import java.util.Date;
import java.util.List;

/**
 * Returns the current date.
 */
public class NowFunction extends BaseFunction {
    @Override
    protected void validateArgs(List<Expression> args) throws TwistException {
        if (!args.isEmpty()) {
            throw new FunctionArgumentException("unexpected arguments: " + args);
        }
    }

    @Override
    protected Date invoke(EvalContext ctx, List<Object> args) {
        return new Date();
    }
}
