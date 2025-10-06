package com.inksetter.twist;

import com.inksetter.twist.expression.function.TwistFunction;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapContext implements EvalContext {
    private final Map<String, Object> values;
    private final Map<String, TwistFunction> functions = new LinkedHashMap<>();
    public MapContext() {
        values = new LinkedHashMap<>();
    }

    public MapContext(Map<String, Object> initial) {
        values = new LinkedHashMap<>(initial);
    }

    @Override
    public boolean isDefined(String name) {
        return values.containsKey(name);
    }

    @Override
    public Object getVariable(String name) {
        return values.get(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        values.put(name, value);
    }

    @Override
    public Map<String, Object> getAll() {
        return values;
    }

    @Override
    public TwistFunction lookupFunction(String name) {
        return functions.get(name);
    }

    @Override
    public void addFunction(String name, TwistFunction func) {
        functions.put(name, func);
    }
}
