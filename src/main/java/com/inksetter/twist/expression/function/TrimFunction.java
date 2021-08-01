package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Trims the argument string.  All trailing whitespace is removed.
 */
public class TrimFunction extends BaseFunction {

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
            return new TwistValue(TwistDataType.STRING, rtrim(strValue));
        }
    }
    
    private static String rtrim(String in) {
        for (int i = in.length(); i > 0; i--) {
            if (in.charAt(i - 1) > ' ') {
                return in.substring(0, i);
            }
        }
        return "";
    }
}
