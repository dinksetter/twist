package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.SimpleScriptContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.function.FunctionArgumentException;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.List;

/**
 * A function defined by a script, either with <code>def</code> or as a lambda. The function keeps
 * the scope it was defined in, so its body sees that scope rather than the caller's.
 * <p>
 * A <code>def</code> function is closed for assignment: setting a variable in its body creates a
 * local, even when an enclosing scope has that name. A lambda is a closure, so assigning to a
 * captured name updates the variable it captured.
 */
public class UserDefFunction implements TwistFunction {
    private final String name;
    private final List<String> argsNames;
    private final StatementBlock body;
    private final ScriptContext captured;
    private final boolean closure;

    public UserDefFunction(String name, List<String> argNames, StatementBlock body, ScriptContext captured, boolean closure) {
        this.name = name;
        this.body = body;
        this.argsNames = argNames;
        this.captured = captured;
        this.closure = closure;
    }

    @Override
    public Object invoke(List<Object> args, EvalContext context) throws TwistException {
        if (args.size() != argsNames.size()) {
            throw new FunctionArgumentException("expected " + argsNames.size() + " arguments");
        }

        ScriptContext scriptContext = newFrame(context);

        // Arguments always belong to the new frame, even when a captured scope has the same name.
        for (int i = 0; i < argsNames.size(); i++) {
            scriptContext.defineLocal(argsNames.get(i), args.get(i));
        }

        return body.execute(scriptContext);
    }

    private ScriptContext newFrame(EvalContext context) {
        ScriptContext enclosing = captured;
        if (enclosing == null) {
            // Defined outside of a script context; fall back to the caller's scope if it has one.
            enclosing = (context instanceof ScriptContext) ? (ScriptContext) context : new SimpleScriptContext();
        }

        return closure ? enclosing.push() : enclosing.pushCall();
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return (name == null || name.isEmpty() ? "lambda" : name) + argsNames;
    }
}
