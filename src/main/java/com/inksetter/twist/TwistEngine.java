package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.parser.TwistParser;

import java.util.Map;

public class TwistEngine {
    private final Map<String, TwistFunction> functions;

    public TwistEngine(Map<String, TwistFunction> functions) {
        this.functions = functions;
    }

    public Expression parseExpression(String expr) throws TwistException {
        return new TwistParser(expr, functions).parseExpression();
    }

    public Script parseScript(String script) throws TwistException {
        return new TwistParser(script, functions).parseScript();
    }
}
