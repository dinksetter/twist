package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.Assignable;
import com.inksetter.twist.expression.TypeMismatchException;

public class ForListStatement implements Statement {
    private final Assignable variable;
    private final Expression listExpr;
    private final Statement body;

    public ForListStatement(Assignable variable, Expression listExpr, Statement body ) {
        this.variable = variable;
        this.listExpr = listExpr;
        this.body = body;
    }

    public StatementResult execute(ScriptContext exec) throws TwistException {
        ScriptContext loop = exec.push();
        Object list = listExpr.evaluate(loop);

        if (list instanceof Iterable<?>) {
            for (Object value : ((Iterable<?>) list)) {
                variable.assignValue(loop, value);
                StatementResult result = body.execute(loop);
                if (result.getType() == StatementResult.Type.RETURN) {
                    return result;
                }
            }
        }
        else {
            throw new TypeMismatchException("not iterable");
        }

        return StatementResult.valueResult(null);
    }
}
