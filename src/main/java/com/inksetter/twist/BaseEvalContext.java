package com.inksetter.twist;

import java.util.List;

public class BaseEvalContext implements EvalContext {
    @Override
    public Object getVariable(String name) {
        return null;
    }

    @Override
    public boolean lookupExternalFunction(String functionName) {
        return false;
    }

    @Override
    public Object invokeExternalFunction(String functionName, List<Object> argValues) {
        return null;
    }
}
