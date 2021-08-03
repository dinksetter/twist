package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the string argument, folded to upper case.
 */
public class UpperFunction  extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) throws TwistException {
        String strValue = ValueUtils.asString(argValue);

        return strValue == null ? null : strValue.toUpperCase();
    }
}
