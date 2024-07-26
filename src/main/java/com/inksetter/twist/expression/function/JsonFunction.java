package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.MapContext;
import com.inksetter.twist.parser.TwistParser;

/**
 * Casts the argument to a string.
 */
public class JsonFunction extends SingleArgFunction {

    @Override
    protected Object invoke(Object argValue) throws TwistException {
        return new TwistParser(argValue.toString()).parseExpression().evaluate(new MapContext());
    }
}
