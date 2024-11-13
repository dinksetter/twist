package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

public class ReturnStatement implements Statement {
    private final Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public StatementResult execute(ScriptContext exec) throws TwistException {
        return StatementResult.returnResult(expression.evaluate(exec));
    }

    public Expression getExpression() {
        return expression;
    }
}
