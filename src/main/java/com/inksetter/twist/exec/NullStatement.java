package com.inksetter.twist.exec;

public class NullStatement implements Statement {

    @Override
    public StatementResult execute(ScriptContext exec) {
        return StatementResult.valueResult(null);
    }
}
