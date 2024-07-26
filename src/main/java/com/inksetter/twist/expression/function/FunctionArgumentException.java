package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;

/**
 * Thrown to indicate a problem with a function argument. Since function
 * arguments are not validated against known type metadata, it is up to the
 * function implementation to perform any argument type and precondition
 * testing, including whether the right number of arguments were passed.
 */
public class FunctionArgumentException extends TwistException {
    public FunctionArgumentException(String message) {
        super("Invalid function argument: " + message);
    }
}
