package com.inksetter.twist.parser;

import com.inksetter.twist.TwistException;

public class ScriptSyntaxException extends TwistException {
    private final int pos;

    ScriptSyntaxException(TwistLexer scan, String message) {
        super("Syntax error at line " + scan.getLine() + "." + scan.getLinePos() + ": " + message);
        this.pos = scan.getPos();
    }

    public int getPos() {
        return pos;
    }
}
