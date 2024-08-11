package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

public class ExpressionStatement implements Statement {
    private final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object execute(ScriptContext exec) throws TwistException {
        return expression.evaluate(exec);
    }

    public Expression getExpression() {
        return expression;
    }
}
