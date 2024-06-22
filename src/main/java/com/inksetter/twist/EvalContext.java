package com.inksetter.twist;

import java.util.List;

public interface EvalContext {

    Object getVariable(String name);

    boolean lookupExternalFunction(String functionName);

    Object invokeExternalFunction(String functionName, List<Object> argValues);
}
