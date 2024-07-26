package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

import java.util.List;

/**
 * Returns the largest of a list of values.
 */
public class MaxFunction extends BaseFunction {

    @Override
    public Object invoke(List<Object> args) {
        Object maxValue = null;

        for (Object a : args) {
            if (maxValue == null || ValueUtils.compare(maxValue, a) < 0) {
                maxValue = a;
            }
        }

        return maxValue;
    }

}
