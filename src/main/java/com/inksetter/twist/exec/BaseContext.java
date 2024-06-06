package com.inksetter.twist.exec;

import java.util.List;

public class BaseContext implements EvalContext {
    @Override
    public Object lookup(String name) {
        return null;
    }

    @Override
    public boolean functionExists(String functionName) {
        return false;
    }

    @Override
    public Object callFunction(String functionName, List<Object> argValues) {
        return null;
    }
}
