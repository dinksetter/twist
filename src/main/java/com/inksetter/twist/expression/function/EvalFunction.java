package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.MapContext;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.parser.TwistParser;

import java.util.List;

/**
 * Evaluates the string as a TWIST expression.
 */
public class EvalFunction implements TwistFunction {
    @Override
    public final Object invoke(List<Object> args, EvalContext context) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("unexpected arguments: " + args);
        }

        // Should we create a new context or use the old one?
        // It comes down to side effects and exposure of functions.
        // e.g. eval("x = 100"), if run in a script context, what would it do?
        return new TwistParser(ValueUtils.asString(args.get(0))).parseExpression().evaluate(new MapContext());
    }
}
