package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;

/**
 * Casts the argument to a string.
 */
public class TypeFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) throws TwistException {
        return ValueUtils.getType(argValue).toString();
    }
}
