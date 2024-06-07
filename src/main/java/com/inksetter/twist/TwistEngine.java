package com.inksetter.twist;

import com.inksetter.twist.exec.SimpleContext;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.parser.TwistParseException;
import com.inksetter.twist.parser.TwistParser;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class TwistEngine {
    private final FunctionResolver functions;
    private final SymbolSource vars;

    public TwistEngine(FunctionResolver funcs, SymbolSource vars) {
        this.functions = funcs;
        this.vars = vars;
    }
    public TwistEngine(SymbolSource vars) {
        this.functions = null;
        this.vars = vars;
    }

    public Object eval(String expression) throws TwistParseException {
        return new TwistParser(expression, functions).parseExpression().evaluate(vars);
    }

    public static class ExprTestObject {
        private final String x = "banana";
        private final int y = 23;


        public String getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static void main(String[] args) throws TwistParseException{
        TwistEngine engine = new TwistEngine(SymbolSource.of(Map.of("foo", new ExprTestObject(), "bar", "banana", "baz", 23)));

        Instant begin = Instant.now();
        for (int i = 0; i < 1000000; i++) {
            ValueUtils.asInt(engine.eval("foo.y / 7"));
            ValueUtils.asString(engine.eval("foo.x.substring(2,2) "));
        }
        Instant end = Instant.now();
        System.out.println("duration: " + Duration.between(begin, end).toMillis());
    }
}
