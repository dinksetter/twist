

package com.inksetter.twist.exec;

import java.util.List;

public interface ExecContext {

    void popStack();

    void pushStack();

    Object getVariable(String name);

    void setVariable(String name, Object value);

    boolean lookupExternalFunction(String functionName);

    Object invokeExternalFunction(String functionName, List<Object> argValues);
}