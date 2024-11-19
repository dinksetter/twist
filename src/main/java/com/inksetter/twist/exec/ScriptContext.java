

package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.expression.function.TwistFunction;

public interface ScriptContext extends EvalContext {
    void popStack();

    void pushStack(boolean fresh);

    void addFunction(String name, TwistFunction function);
}