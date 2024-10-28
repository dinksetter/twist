package com.inksetter.twist.parser;

public class ScriptTokenException extends ScriptSyntaxException {
    ScriptTokenException(TwistLexer scan, String message) {
        super(scan, message);
    }
}
