

package com.inksetter.twist.exec;

import com.inksetter.twist.TwistValue;

import java.util.List;

public interface ExecContext {

    void popStack();

    void pushStack();

    TwistValue getVariable(String name);

    void setVariable(String name, TwistValue value);

    boolean lookupExternalFunction(String functionName);

    TwistValue invokeExternalFunction(String functionName, List<TwistValue> argValues);
}