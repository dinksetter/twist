package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Casts the argument to an integer value.
 */
public class IntFunction extends SingleArgFunction {

    @Override
    protected Integer invoke(Object argValue) {
        if (ValueUtils.isNull(argValue)) {
            return null;
        }
        else {
            return ValueUtils.asInt(argValue);
        }
    }
}
