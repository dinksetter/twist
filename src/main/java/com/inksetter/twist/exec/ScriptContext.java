

package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;

public interface ScriptContext extends EvalContext {

    void popStack();

    void pushStack();

    void setVariable(String name, Object value);

    void debug(String format, Object... args);
}