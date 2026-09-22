package com.inksetter.twist;

import com.inksetter.twist.parser.ScriptSyntaxException;
import com.inksetter.twist.parser.TwistParser;

import java.util.Map;

public class Twist {
    public static Expression parseExpression(String expr) throws TwistException {
        return new TwistParser(expr).parseExpression();
    }

    public static Script parseScript(String script) throws ScriptSyntaxException {
        return new TwistParser(script).parseScript();
    }

    public static <T> T eval(String expr, Map<String, Object> contextData, Class<T> cls) throws TwistException {
        return new TwistParser(expr).parseExpression().evaluate(new MapContext(contextData), cls);
    }

    public static Object eval(String expr, Map<String, Object> contextData) throws TwistException {
        return new TwistParser(expr).parseExpression().evaluate(new MapContext(contextData));
    }

    public static Object exec(String script, Map<String, Object> contextData) throws TwistException {
        return new TwistParser(script).parseScript().execute(new SimpleScriptContext(contextData, Map.of()));
    }

}
