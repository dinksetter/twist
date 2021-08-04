package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

/**
 * Casts the argument to a string.
 */
public class StringFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) throws TwistException {
        return ValueUtils.asString(argValue);
    }
}
