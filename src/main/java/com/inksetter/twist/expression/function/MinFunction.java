package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

import java.util.List;

/**
 * Returns the smallest of a list of values.
 */
public class MinFunction extends BaseFunction {
    @Override
    public Object invoke(List<Object> args) {
        Object minValue = null;
        
        for (Object a : args) {
            if (minValue == null || ValueUtils.compare(minValue, a) > 0) {
                minValue = a;
            }
        }
        
        return minValue;
    }
}
