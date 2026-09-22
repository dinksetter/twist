package com.inksetter.twist.exec;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.expression.function.TwistFunction;

/**
 * A scope that a script executes in. A context is a single stack frame: it holds its own variables
 * and refers to the frame that encloses it, so a nested scope is a new context rather than a change
 * to an existing one. Nothing has to be popped; a frame simply goes out of use.
 */
public interface ScriptContext extends EvalContext {

    /**
     * Returns a nested frame for a block, loop or catch clause. Assigning to a name that already
     * exists in an enclosing frame updates it there.
     */
    ScriptContext push();

    /**
     * Returns a nested frame for the body of a named function. Assignment stops at this frame, so a
     * function's variables are local to it even when an enclosing frame has the same name.
     */
    ScriptContext pushCall();

    /**
     * Sets a variable in this frame, without looking for it in enclosing frames. Used for things
     * that always belong to the new scope, such as function arguments and catch variables.
     */
    void defineLocal(String name, Object value);

    void addFunction(String name, TwistFunction function);
}
