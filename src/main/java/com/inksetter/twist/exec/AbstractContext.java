package com.inksetter.twist.exec;

import java.util.*;

public class AbstractContext implements ExecContext {

    private final Deque<Map<String, Object>> _stackFrames = new LinkedList<>();

    public AbstractContext() {
        _stackFrames.addFirst(new LinkedHashMap<>());
    }

    @Override
    public void popStack() {
        _stackFrames.removeFirst();
    }

    @Override
    public void pushStack() {
        _stackFrames.addFirst(new LinkedHashMap<>());
    }

    @Override
    public Object getVariable(String name) {
        for (Map<String, Object> symbols : _stackFrames) {
            if (symbols.containsKey(name)) {
                return symbols.get(name);
            }
        }
        return null;
    }

    @Override
    public void setVariable(String name, Object value) {
        for (Map<String, Object> symbols : _stackFrames) {
            if (symbols.containsKey(name)) {
                symbols.put(name, value);
                return;
            }
        }
        _stackFrames.getFirst().put(name, value);
    }

    @Override
    public boolean lookupExternalFunction(String functionName) {
        return false;
    }

    @Override
    public Object invokeExternalFunction(String functionName, List<Object> argValues) {
        return null;
    }

    @Override
    public void debug(String format, Object... args) {
        // by default, do nothing
    }
}
