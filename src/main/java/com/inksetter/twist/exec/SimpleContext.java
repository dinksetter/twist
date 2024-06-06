package com.inksetter.twist.exec;

import java.util.*;

public class SimpleContext extends BaseContext  {

    private final Map<String, Object> symbols = new LinkedHashMap<>();

    public SimpleContext(Map<String, Object> data) {
        symbols.putAll(data);
    }

    public SimpleContext() {
    }

    @Override
    public Object lookup(String name) {
        return symbols.get(name);
    }

    public void setVariable(String name, Object value) {
        symbols.put(name, value);
    }
}
