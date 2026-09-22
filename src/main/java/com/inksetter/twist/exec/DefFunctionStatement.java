package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

import java.util.List;

public class DefFunctionStatement implements Statement {
    private final String name;
    private final List<String> argNames;
    private final StatementBlock body;

    public DefFunctionStatement(String name, List<String> argNames, StatementBlock body) {
        this.name = name;
        this.argNames = argNames;
        this.body = body;
    }

    @Override
    public StatementResult execute(ScriptContext exec) throws TwistException {
        // The function keeps the scope it was defined in, but its body gets its own locals.
        exec.addFunction(name, new UserDefFunction(name, argNames, body, exec, false));
        return StatementResult.valueResult(null);
    }
}
