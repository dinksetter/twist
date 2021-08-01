package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the largest of a list of values.
 */
public class MaxFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() == 0) {
            throw new FunctionArgumentException("expected arguments");
        }
        
        TwistValue maxValue = null;
        
        for (TwistValue a : args) {
            if (maxValue == null || maxValue.asDouble() < a.asDouble()) {
                maxValue = a;
            }
        }
        
        if (maxValue == null) {
            maxValue = new TwistValue(TwistDataType.INTEGER, null);
        }
        
        return maxValue;
    }
}
