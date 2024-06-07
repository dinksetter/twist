package com.inksetter.twist;

import com.inksetter.twist.expression.function.GenericFunction;

public interface FunctionResolver {
    GenericFunction lookupFunction(String name);
}
