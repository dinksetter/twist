package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.Date;

/**
 * Casts the argument as a date.
 */
public class DateFunction extends SingleArgFunction {

    @Override
    public Date invoke(Object arg) throws TwistException {
        return ValueUtils.asDate(arg);
    }
}
