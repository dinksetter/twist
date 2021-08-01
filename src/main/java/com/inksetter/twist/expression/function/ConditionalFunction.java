package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

/**
 * Implements the <code>iif</code> function. Generally, this function
 * takes three arguments. The first argument is evaluated as a boolean. If it
 * returns <code>true</code>, the second argument is returned, otherwise, the
 * third argument is returned.  If only two arguments are passed, and the first
 * argument is <code>false</code>, NULL is returned.
 */
public class ConditionalFunction implements TwistFunction {

    @Override
    public TwistValue evaluate(ExecContext ctx, List<Expression> args) throws TwistException {
        if (args.size() < 2 || args.size() > 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
        
        TwistValue test = args.get(0).evaluate(ctx);
        
        if (test.asBoolean()) {
            return args.get(1).evaluate(ctx);
        }
        else {
            if (args.size() == 3) {
                return args.get(2).evaluate(ctx);
            }
            else {
                return new TwistValue(TwistDataType.STRING, null);
            }
        }
    }
}
