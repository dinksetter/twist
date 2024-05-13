package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Trims the argument string.  All trailing whitespace is removed.
 */
public class TrimFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) {
        String strValue = ValueUtils.asString(argValue);

        return strValue == null ? null : strValue.trim();
    }
}
