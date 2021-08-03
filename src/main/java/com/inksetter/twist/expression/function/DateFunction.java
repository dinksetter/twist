package com.inksetter.twist.expression.function;

import java.util.Date;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

/**
 * Casts the argument as a date.
 */
public class DateFunction extends SingleArgFunction {

    @Override
    protected Date invoke(Object arg) {
        return ValueUtils.asDate(arg);
    }
}
