package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the string argument, folded to lower case.
 */
public class LowerFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        TwistValue argValue = args.get(0);
        String strValue = argValue.asString();

        if (strValue == null) {
            return new TwistValue(TwistDataType.STRING, null);
        }
        else {
            return new TwistValue(TwistDataType.STRING, strValue.toLowerCase());
        }
    }
}
