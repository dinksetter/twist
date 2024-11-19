package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;

import java.util.Date;
import java.util.List;

/**
 * Returns the current date.
 */
public class NowFunction extends BaseFunction {
    @Override
    public Date invoke(List<Object> args, EvalContext context) throws FunctionArgumentException {
        if (!args.isEmpty()) {
            throw new FunctionArgumentException("unexpected arguments: " + args);
        }
        return new Date();
    }
}
