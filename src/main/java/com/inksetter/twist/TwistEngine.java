package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.parser.ScriptSyntaxException;
import com.inksetter.twist.parser.TwistParser;

import java.util.HashMap;
import java.util.Map;

public class TwistEngine {

    public Expression parseExpression(String expr) throws TwistException {
        return new TwistParser(expr, this).parseExpression();
    }

    public Script parseScript(String script) throws ScriptSyntaxException {
        return new TwistParser(script, this).parseScript();
    }
}
