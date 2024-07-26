package com.inksetter.twist.parser;

public class ScriptTokenException extends ScriptSyntaxException {
    public ScriptTokenException(int line, int linePos, String message) {
        super(line, linePos, message);
    }
}
