package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the length of the string argument, in characters.
 */
public class LengthFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
        TwistValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new TwistValue(TwistDataType.INTEGER, 0);
        }
        else {
            return new TwistValue(TwistDataType.INTEGER, argValue.asString().length());
        }
    }
}
