package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.parser.ScriptSyntaxException;
import com.inksetter.twist.parser.TwistParser;

import java.util.HashMap;
import java.util.Map;

public class TwistEngine {
    private final Map<String, TwistFunction> functions = new HashMap<>();

    public TwistEngine(Map<String, TwistFunction> functions) {
        this.functions.putAll(functions);
    }

    public TwistEngine() {
    }

    public void addFunction(String name, TwistFunction function) {
        functions.put(name, function);
    }

    public TwistFunction lookupFunction(String name) {
        return functions.get(name);
    }

    public Expression parseExpression(String expr) throws TwistException {
        return new TwistParser(expr, this).parseExpression();
    }

    public Script parseScript(String script) throws ScriptSyntaxException {
        return new TwistParser(script, this).parseScript();
    }
}
