package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.exec.UserDefFunction;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.List;

public class LambdaExpression implements Expression {
    private final UserDefFunction function;
    public LambdaExpression(List<String> identifiers, StatementBlock body) {
        this.function = new UserDefFunction("", identifiers, body);
    }

    public UserDefFunction evaluate(EvalContext ctx) {
        return function;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "-> " + function;
    }
}
