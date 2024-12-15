package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

public class IfStatement implements Statement {
    private final Expression ifTest;
    private final Statement ifStatement;
    private final Statement elseStatement;

    public IfStatement(Expression ifTest, Statement ifStatement, Statement elseStatement) {
        this.ifTest = ifTest;
        this.ifStatement = ifStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public StatementResult execute(ScriptContext exec) throws TwistException {
        Object testValue = ifTest.evaluate(exec);
        if (ValueUtils.asBoolean(testValue)) {
            return ifStatement.execute(exec);
        }
        else if (elseStatement != null) {
            return elseStatement.execute(exec);
        }

        // If statements not executed have a null value
        return StatementResult.valueResult(null);
    }
}
