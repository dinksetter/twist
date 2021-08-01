package com.inksetter.twist.parser;

public class TwistLexException extends TwistParseException {
    public TwistLexException(int line, int linePos, String message) {
        super(line, linePos, message);
    }
}
