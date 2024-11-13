package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

public class DefFunctionStatement implements Statement {
    private final String name;
    private final UserDefFunction function;
    public DefFunctionStatement(String name, UserDefFunction function) {
        this.name = name;
        this.function = function;
    }

    @Override
    public StatementResult execute(ScriptContext exec) throws TwistException {
        exec.addFunction(name ,function);
        return StatementResult.valueResult(null);
    }
}
