package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

/**
 * Returns the smallest of a list of values.
 */
public class MinFunction extends BaseFunction {
    @Override
    protected Object invoke(ExecContext ctx, List<Object> args) throws TwistException {
        Object minValue = null;
        
        for (Object a : args) {
            if (minValue == null || ValueUtils.compare(minValue, a) > 0) {
                minValue = a;
            }
        }
        
        return minValue;
    }
}
