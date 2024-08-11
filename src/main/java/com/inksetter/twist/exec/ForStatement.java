package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

public class ForStatement implements Statement {
    private final Expression initial;
    private final Expression testExpr;
    private final Expression endExpr;
    private final Statement body;

    public ForStatement(Expression initial, Expression testExpr, Expression endExpr, Statement body ) {
        this.initial = initial;
        this.testExpr = testExpr;
        this.endExpr = endExpr;
        this.body = body;
    }

    public Object execute(ScriptContext exec) throws TwistException {
        for (initial.evaluate(exec);
             ValueUtils.asBoolean(testExpr.evaluate(exec));
             endExpr.evaluate(exec)) {
            body.execute(exec);
        }

        // for statements do not have value, just side effects.
        return null;
    }
}
