package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.function.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An expression that represents a function. Functions are evaluated
 * in-line as values, and there's support for calling user-supplied functions.
 */
public class FunctionExpression implements Expression {
    private final String name;
    private final List<Expression> args;

    public FunctionExpression(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }
    
    public Object evaluate(EvalContext ctx) throws TwistException {
        TwistFunction func = BUILTINS.get(name.toLowerCase());

        if (func == null) {
            func = ctx.lookupFunction(name);
        }

        if (func == null) {
            throw new TwistException("unrecognized function: " + name);
        }

        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return func.invoke(argValues);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(name);
        tmp.append('(');
        boolean firstOne = true;
        for (Expression arg : args) {
            if (firstOne) {
                firstOne = false;
            }
            else {
                tmp.append(',');
            }
            tmp.append(arg);
        }
        tmp.append(')');
        return tmp.toString();
    }

    private final static Map<String, TwistFunction> BUILTINS = new HashMap<>();
    static {
        BUILTINS.put("date", new DateFunction());
        BUILTINS.put("string", new StringFunction());
        BUILTINS.put("int", new IntFunction());
        BUILTINS.put("double", new DoubleFunction());
        BUILTINS.put("upper", new UpperFunction());
        BUILTINS.put("lower", new LowerFunction());
        BUILTINS.put("trim", new TrimFunction());
        BUILTINS.put("len", new LengthFunction());
        BUILTINS.put("length", new LengthFunction());
        BUILTINS.put("sprintf", new SprintfFunction());
        BUILTINS.put("min", new MinFunction());
        BUILTINS.put("max", new MaxFunction());
        BUILTINS.put("substr", new SubstrFunction());
        BUILTINS.put("indexof", new IndexOfFunction());
        BUILTINS.put("eval", new EvalFunction());
        BUILTINS.put("b64decode", new Base64DecodeFunction());
        BUILTINS.put("b64encode", new Base64EncodeFunction());
        BUILTINS.put("now", new NowFunction());
        BUILTINS.put("type", new TypeFunction());
    }
}
