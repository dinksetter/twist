package com.inksetter.twist;

import java.util.List;

public interface EvalContext {
    Object getVariable(String name);
    void setVariable(String name, Object value);
}
