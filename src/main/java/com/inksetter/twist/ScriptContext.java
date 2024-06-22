

package com.inksetter.twist;

public interface ScriptContext extends EvalContext {

    void popStack();

    void pushStack();

    void setVariable(String name, Object value);

    void debug(String format, Object... args);
}