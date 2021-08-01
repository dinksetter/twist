package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the smallest of a list of values.
 */
public class MinFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() == 0) {
            throw new FunctionArgumentException("expected arguments");
        }
        
        TwistValue minValue = null;
        
        for (TwistValue a : args) {
            if (minValue == null || minValue.asDouble() > a.asDouble()) {
                minValue = a;
            }
        }
        
        if (minValue == null) {
            minValue = new TwistValue(TwistDataType.INTEGER, null);
        }
        
        return minValue;
    }
}
