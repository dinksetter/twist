package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Casts the argument as a double-precision floating point number.
 */
public class DoubleFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        TwistValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new TwistValue(TwistDataType.DOUBLE, null);
        }
        else {
            return new TwistValue(TwistDataType.DOUBLE, args.get(0).asDouble());
        }
    }
}
