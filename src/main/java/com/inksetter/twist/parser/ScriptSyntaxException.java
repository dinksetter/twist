package com.inksetter.twist.parser;

import com.inksetter.twist.TwistException;

public class ScriptSyntaxException extends TwistException {

    public ScriptSyntaxException(int line, int linePos, String detail) {
        super("Syntax error at line " + line + "." + linePos + ": " + detail);
    }
}
