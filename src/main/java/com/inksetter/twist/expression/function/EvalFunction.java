package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.MapContext;
import com.inksetter.twist.parser.TwistParser;

/**
 * Evaluates the string as a TWIST expression.
 */
public class EvalFunction extends SingleArgFunction {
    @Override
    protected Object invoke(Object argValue) throws TwistException {
        return new TwistParser(argValue.toString()).parseExpression().evaluate(new MapContext());
    }
}
