package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Casts the argument to a string.
 */
public class StringFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) {
        return ValueUtils.asString(argValue);
    }
}
