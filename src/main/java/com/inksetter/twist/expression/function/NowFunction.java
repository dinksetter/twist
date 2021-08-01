package com.inksetter.twist.expression.function;

import java.util.Date;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the current date.
 */
public class NowFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        return new TwistValue(TwistDataType.DATETIME, new Date());
    }
}
