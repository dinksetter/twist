package com.inksetter.twist;

import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.parser.TwistParseException;
import com.inksetter.twist.parser.TwistParser;

public class TwistEngine {
    private static FunctionResolver functions = null;
    public static Object eval(String expression, SymbolSource ctx) throws TwistParseException {
        return new TwistParser(expression, functions).parseExpression().evaluate(ctx);
    }

    public static void setFunctionResolver(FunctionResolver functions) {
        TwistEngine.functions = functions;
    }
}
