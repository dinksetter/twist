package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;

import java.util.List;

public interface GenericFunction {
    Object invoke(List<Object> argValues) throws TwistException;
}
