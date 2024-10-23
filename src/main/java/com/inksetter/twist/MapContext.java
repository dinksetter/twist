package com.inksetter.twist;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapContext implements EvalContext {
    private final Map<String, Object> values;
    public MapContext() {
        values = new LinkedHashMap<>();
    }
    public MapContext(Map<String, Object> initial) {
        values = initial;
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
}
