package com.inksetter.twist.expression.function;

import java.util.Date;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

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
    protected Date invoke(ExecContext ctx, List<Object> args) throws TwistException {
        return new Date();
    }
}
