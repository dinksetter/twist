package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;

/**
 * Casts the argument as a double-precision floating point number.
 */
public class DoubleFunction extends SingleArgFunction {

    @Override
    protected Double invoke(Object argValue) throws TwistException {
        return ValueUtils.asDouble(argValue);
    }
}
