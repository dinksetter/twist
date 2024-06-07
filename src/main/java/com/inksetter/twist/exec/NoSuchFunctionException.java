package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

public class NoSuchFunctionException extends TwistException {
    public NoSuchFunctionException(String name) {
        super("no such function: " + name);
    }
}
