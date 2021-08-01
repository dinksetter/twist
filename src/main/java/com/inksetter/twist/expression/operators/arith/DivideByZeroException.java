package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistException;

/**
 * Thrown when division by zero is attempted.
 */
public class DivideByZeroException extends TwistException {
    public DivideByZeroException() {
        super("Divide by zero");
    }
    
}
