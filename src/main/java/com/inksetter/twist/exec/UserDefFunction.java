package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.SimpleScriptContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.function.FunctionArgumentException;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.List;

public class UserDefFunction implements TwistFunction {
    private final String name;
    private final List<String> argsNames;
    private final StatementBlock body;

    public UserDefFunction(String name, List<String> argNames, StatementBlock body) {
        this.name = name;
        this.body = body;
        this.argsNames = argNames;
    }

    @Override
    public Object invoke(List<Object> args) throws TwistException {
        ScriptContext scriptContext = new SimpleScriptContext();
        if (args.size() != argsNames.size()) {
            throw new FunctionArgumentException("expected " + argsNames.size() + " arguments");
        }
        for (int i = 0; i < argsNames.size(); i++) {
            scriptContext.setVariable(argsNames.get(i), args.get(i));
        }

        return body.execute(scriptContext);
    }
}
