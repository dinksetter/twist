package com.inksetter.twist;

import com.inksetter.twist.parser.ScriptSyntaxException;
import com.inksetter.twist.parser.TwistParser;

public class TwistEngine {

    public Expression parseExpression(String expr) throws TwistException {
        return new TwistParser(expr).parseExpression();
    }

    public Script parseScript(String script) throws ScriptSyntaxException {
        return new TwistParser(script).parseScript();
    }
}
