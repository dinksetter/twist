package com.inksetter.twist.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.function.Base64DecodeFunction;
import com.inksetter.twist.expression.function.Base64EncodeFunction;
import com.inksetter.twist.expression.function.ExternalFunction;
import com.inksetter.twist.expression.function.ConditionalFunction;
import com.inksetter.twist.expression.function.DateFunction;
import com.inksetter.twist.expression.function.DoubleFunction;
import com.inksetter.twist.expression.function.IndexOfFunction;
import com.inksetter.twist.expression.function.IntFunction;
import com.inksetter.twist.expression.function.LengthFunction;
import com.inksetter.twist.expression.function.LowerFunction;
import com.inksetter.twist.expression.function.MaxFunction;
import com.inksetter.twist.expression.function.MinFunction;
import com.inksetter.twist.expression.function.TwistFunction;
import com.inksetter.twist.expression.function.IfNullFunction;
import com.inksetter.twist.expression.function.SprintfFunction;
import com.inksetter.twist.expression.function.StringFunction;
import com.inksetter.twist.expression.function.SubstrFunction;
import com.inksetter.twist.expression.function.NowFunction;

import com.inksetter.twist.expression.function.TrimFunction;
import com.inksetter.twist.expression.function.TypeFunction;
import com.inksetter.twist.expression.function.UpperFunction;

/**
 * An expression that represents a function. Functions are evaluated
 * in-line as values, and there's support for calling user-supplied functions.
 */
public class FunctionExpression implements Expression {
    
    public static FunctionExpression getBuiltInFunction(String name, List<Expression> args) {
        TwistFunction function = _FUNCTIONS.get(name.toLowerCase());
        if (function != null) {
            return new FunctionExpression(name, args, function);
        }
        else {
            return null;
        }
    }
    
    public static FunctionExpression getServiceFunction(String name, List<Expression> args) {
        return new FunctionExpression(name, args, new ExternalFunction(name));
    }
    
    private FunctionExpression(String name, List<Expression> args, TwistFunction function) {
        _name = name;
        _args = args;
        _function = function;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
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
    
    private final static Map<String, TwistFunction> _FUNCTIONS = new HashMap<>();
    static {
        _FUNCTIONS.put("date", new DateFunction());
        _FUNCTIONS.put("string", new StringFunction());
        _FUNCTIONS.put("int", new IntFunction());
        _FUNCTIONS.put("float", new DoubleFunction());
        _FUNCTIONS.put("upper", new UpperFunction());
        _FUNCTIONS.put("lower", new LowerFunction());
        _FUNCTIONS.put("trim", new TrimFunction());
        _FUNCTIONS.put("rtrim", new TrimFunction());
        _FUNCTIONS.put("len", new LengthFunction());
        _FUNCTIONS.put("length", new LengthFunction());
        _FUNCTIONS.put("sprintf", new SprintfFunction());
        _FUNCTIONS.put("iif", new ConditionalFunction());
        _FUNCTIONS.put("nvl", new IfNullFunction());
        _FUNCTIONS.put("min", new MinFunction());
        _FUNCTIONS.put("max", new MaxFunction());
        _FUNCTIONS.put("substr", new SubstrFunction());
        _FUNCTIONS.put("instr", new IndexOfFunction());
        _FUNCTIONS.put("b64decode", new Base64DecodeFunction());
        _FUNCTIONS.put("b64encode", new Base64EncodeFunction());
        _FUNCTIONS.put("now", new NowFunction());
        _FUNCTIONS.put("type", new TypeFunction());
    }
}
