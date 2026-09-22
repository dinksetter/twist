package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.exec.UserDefFunction;
import com.inksetter.twist.expression.function.ExpressionFunction;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.ArrayList;
import java.util.List;

public class CallExpression implements Expression {
    private final Expression expr;
    private final List<Expression> args;

    public CallExpression(Expression expr, List<Expression> args) {
        this.expr = expr;
        this.args = args;
    }

    public Object evaluate(EvalContext ctx) throws TwistException {
        TwistFunction func = expr.evaluate(ctx, TwistFunction.class);

        if (func == null) {
            throw new TwistException("not callable ");
        }

        // Some functions want to see their arguments as expressions, rather than as values.
        if (func instanceof ExpressionFunction) {
            return ((ExpressionFunction) func).invokeRaw(args, ctx);
        }

        List<Object> argValues = new ArrayList<>();

        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }

        return func.invoke(argValues, ctx);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(expr);
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
