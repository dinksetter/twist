package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

/**
 * Returns the string argument, folded to upper case.
 */
public class UpperFunction  extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) {
        String strValue = ValueUtils.asString(argValue);

        return strValue == null ? null : strValue.toUpperCase();
    }
}
