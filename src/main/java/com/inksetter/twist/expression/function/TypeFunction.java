package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

/**
 * Casts the argument to a string.
 */
public class TypeFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) {
        return ValueUtils.getType(argValue).toString();
    }
}
