package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.EvalContext;
import com.inksetter.twist.expression.Expression;

import java.util.List;

/**
 * Simulates the MySQL IFNULL function.  If the first argument
 * is not null, it is returned.  Otherwise, the second argument is
 * returned. Note, the second argument is not even evaluated if the
 * first argument is null.
 */
public class IfNullFunction implements TwistFunction {

    @Override
    public Object evaluate(EvalContext ctx, List<Expression> args) throws TwistException {
        if (args.size() != 2) {
            throw new FunctionArgumentException("expected 2 arguments");
        }

        Object test = args.get(0).evaluate(ctx);

        if (!ValueUtils.isNull(test)) {
            return test;
        }
        else {
            return args.get(1).evaluate(ctx);
        }
    }
    
}
