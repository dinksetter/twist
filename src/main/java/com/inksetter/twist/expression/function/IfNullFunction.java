package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

/**
 * Simulates the MySQL IFNULL function.  If the first argument
 * is not null, it is returned.  Otherwise, the second argument is
 * returned.
 */
public class IfNullFunction implements TwistFunction {

    @Override
    public TwistValue evaluate(ExecContext ctx, List<Expression> args) throws TwistException {
        if (args.size() != 2) {
            throw new FunctionArgumentException("expected 2 arguments");
        }
        
        TwistValue test = args.get(0).evaluate(ctx);
        
        if (!test.isNull()) {
            return test;
        }
        else {
            return args.get(1).evaluate(ctx);
        }
    }
    
}
