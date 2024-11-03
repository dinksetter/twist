package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;

import java.util.Map;

public interface EvalContext {
    boolean isDefined(String name);
    Object getVariable(String name);
    void setVariable(String name, Object value);
    TwistFunction lookupFunction(String name);
    void addFunction(String name, TwistFunction func);
    Map<String, Object> getAll();
}
