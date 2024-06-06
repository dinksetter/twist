package com.inksetter.twist.parser;

import com.inksetter.twist.TwistException;

public class TwistParseException extends Exception {

    public TwistParseException(int line, int linePos, String detail) {
        super("Syntax error at line " + line + "." + linePos + ": " + detail);
    }
}
