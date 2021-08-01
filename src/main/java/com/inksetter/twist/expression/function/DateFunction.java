package com.inksetter.twist.expression.function;

import java.util.Date;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Casts the argument as a date.
 */
public class DateFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
        
        TwistValue arg = args.get(0);
        
        Date d = arg.asDate();
        if (d == null && !arg.isNull()) {
            throw new FunctionArgumentException("Invalid date: " + arg.asString());
        }
        
        return new TwistValue(TwistDataType.DATETIME, d);
    }
}
