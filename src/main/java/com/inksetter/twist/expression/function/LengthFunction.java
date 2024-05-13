package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Returns the length of the string argument, in characters.
 */
public class LengthFunction extends SingleArgFunction {

    @Override
    protected Integer invoke(Object argValue) {
        if (ValueUtils.isNull(argValue)) {
            return 0;
        }
        else {
            return ValueUtils.asString(argValue).length();
        }
    }
}
