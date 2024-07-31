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
    private final String name;
    private final List<Expression> args;
    private final TwistFunction function;

    public FunctionExpression(String name, List<Expression> args, TwistFunction function) {
        this.name = name;
        this.args = args;
        this.function = function;
    }
    
    public Object evaluate(EvalContext ctx) throws TwistException {
        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return function.invoke(argValues);
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

}
