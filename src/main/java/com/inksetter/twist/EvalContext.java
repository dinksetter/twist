package com.inksetter.twist;

import java.util.Map;

public interface EvalContext {
    boolean isDefined(String name);
    Object getVariable(String name);
    void setVariable(String name, Object value);
    Map<String, Object> getAll();
}
