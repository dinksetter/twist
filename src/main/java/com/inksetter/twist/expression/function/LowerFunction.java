package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Returns the string argument, folded to lower case.
 */
public class LowerFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) {
        String strValue = ValueUtils.asString(argValue);

        return strValue == null ? null : strValue.toLowerCase();
    }
}
