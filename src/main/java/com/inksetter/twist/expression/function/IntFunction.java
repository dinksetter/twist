package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Casts the argument to an integer value.
 */
public class IntFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
        
        TwistValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new TwistValue(TwistDataType.INTEGER, null);
        }
        else {
            return new TwistValue(TwistDataType.INTEGER, argValue.asInt());
        }
    }
}
