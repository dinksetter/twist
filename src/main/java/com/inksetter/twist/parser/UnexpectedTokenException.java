package com.inksetter.twist.parser;

public class UnexpectedTokenException extends ScriptSyntaxException {
    private final TwistTokenType[] expected;
    private final TwistTokenType token;

    UnexpectedTokenException(TwistLexer scan, TwistTokenType[] allowedTokens) {
        super(scan, "unexpected token: " + scan.tokenType());
        this.token = scan.tokenType();
        this.expected = allowedTokens;
    }

    public TwistTokenType getToken() {
        return token;
    }

    public TwistTokenType[] getExpected() {
        return expected;
    }
}
