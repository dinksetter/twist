package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

import java.util.Date;

/**
 * Casts the argument as a date.
 */
public class DateFunction extends SingleArgFunction {

    @Override
    protected Date invoke(Object arg) {
        return ValueUtils.asDate(arg);
    }
}
