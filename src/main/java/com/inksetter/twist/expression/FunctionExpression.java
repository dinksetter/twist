package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.expression.function.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An expression that represents a function. Functions are evaluated
 * in-line as values, and there's support for calling user-supplied functions.
 */
public class FunctionExpression implements Expression {

    public FunctionExpression(String name, List<Expression> args, TwistFunction function) {
        _name = name;
        _args = args;
        _function = function;
    }
    
    public Object evaluate(SymbolSource ctx) throws TwistException {
        return _function.evaluate(ctx, _args);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(_name);
        tmp.append('(');
        boolean firstOne = true;
        for (Expression arg : _args) {
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

    
    private final String _name;
    private final List<Expression> _args;
    private final TwistFunction _function;

    public static TwistFunction chooseFunction(String name) {
        return _BUILTINS.get(name.toLowerCase());
    }

    private final static Map<String, TwistFunction> _BUILTINS = new HashMap<>();
    static {
        _BUILTINS.put("date", new DateFunction());
        _BUILTINS.put("string", new StringFunction());
        _BUILTINS.put("int", new IntFunction());
        _BUILTINS.put("double", new DoubleFunction());
        _BUILTINS.put("upper", new UpperFunction());
        _BUILTINS.put("lower", new LowerFunction());
        _BUILTINS.put("trim", new TrimFunction());
        _BUILTINS.put("len", new LengthFunction());
        _BUILTINS.put("length", new LengthFunction());
        _BUILTINS.put("sprintf", new SprintfFunction());
        _BUILTINS.put("ifnull", new IfNullFunction());
        _BUILTINS.put("min", new MinFunction());
        _BUILTINS.put("max", new MaxFunction());
        _BUILTINS.put("substr", new SubstrFunction());
        _BUILTINS.put("instr", new IndexOfFunction());
        _BUILTINS.put("b64decode", new Base64DecodeFunction());
        _BUILTINS.put("b64encode", new Base64EncodeFunction());
        _BUILTINS.put("now", new NowFunction());
        _BUILTINS.put("type", new TypeFunction());
    }
}
