

package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;

public interface ScriptContext extends EvalContext {
    void popStack();

    void pushStack();
}