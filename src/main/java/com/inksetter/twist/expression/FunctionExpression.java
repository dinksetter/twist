package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.function.*;

import java.util.ArrayList;
import java.util.List;

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
    
    public Object evaluate(EvalContext ctx) throws TwistException {
        List<Object> argValues = new ArrayList<>();

        for (Expression arg : _args) {
            argValues.add(arg.evaluate(ctx));
        }

        return _function.invoke(argValues);
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
}
