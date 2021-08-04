package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;

public class UnrecognizedMethodException extends TwistException {
    public UnrecognizedMethodException(String memberName) {
        super("unrecognized method: " + memberName);
    }
}
