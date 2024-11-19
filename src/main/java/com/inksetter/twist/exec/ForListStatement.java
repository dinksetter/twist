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
        exec.pushStack(false);
        try {
            Object list = listExpr.evaluate(exec);

            if (list instanceof Iterable<?>) {
                for (Object value : ((Iterable<?>) list)) {
                    variable.assignValue(exec, value);
                    StatementResult result = body.execute(exec);
                    if (result.getType() == StatementResult.Type.RETURN) {
                        return result;
                    }
                }
            }
            else {
                throw new TypeMismatchException("not iterable");
            }
        }
        finally {
            exec.popStack();
        }

        return StatementResult.valueResult(null);
    }
}
