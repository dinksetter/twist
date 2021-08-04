package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

/**
 * Casts the argument as a double-precision floating point number.
 */
public class DoubleFunction extends SingleArgFunction {

    @Override
    protected Double invoke(Object argValue) throws TwistException {
        return ValueUtils.asDouble(argValue);
    }
}
