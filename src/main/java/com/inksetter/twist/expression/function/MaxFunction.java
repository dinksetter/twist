package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;

import java.util.List;

/**
 * Returns the largest of a list of values.
 */
public class MaxFunction extends BaseFunction {

    @Override
    protected Object invoke(ExecContext ctx, List<Object> args) throws TwistException {
        Object maxValue = null;

        for (Object a : args) {
            if (maxValue == null || ValueUtils.compare(maxValue, a) < 0) {
                maxValue = a;
            }
        }

        return maxValue;
    }

}
