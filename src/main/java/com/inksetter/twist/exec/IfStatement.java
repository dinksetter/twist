package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.io.Serializable;
import java.util.List;

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
    public Object execute(ScriptContext exec) throws TwistException {
        Object testValue = ifTest.evaluate(exec);
        if (ValueUtils.asBoolean(testValue)) {
            ifStatement.execute(exec);
        }
        else if (elseStatement != null) {
            elseStatement.execute(exec);
        }

        // If statements do not have value, just side effects.
        return null;
    }
}
