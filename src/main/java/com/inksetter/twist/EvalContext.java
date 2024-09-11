package com.inksetter.twist;

public interface EvalContext {
    boolean isDefined(String name);
    Object getVariable(String name);
    void setVariable(String name, Object value);
}
