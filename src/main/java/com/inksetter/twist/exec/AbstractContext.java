package com.inksetter.twist.exec;

import com.inksetter.twist.TwistValue;

import java.util.*;

public class AbstractContext implements ExecContext {

    private final Deque<Map<String, TwistValue>> _stackFrames = new LinkedList<>();

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
    public TwistValue getVariable(String name) {
        for (Map<String, TwistValue> symbols : _stackFrames) {
            if (symbols.containsKey(name)) {
                return symbols.get(name);
            }
        }
        return null;
    }

    @Override
    public void setVariable(String name, TwistValue value) {
        for (Map<String, TwistValue> symbols : _stackFrames) {
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
    public TwistValue invokeExternalFunction(String functionName, List<TwistValue> argValues) {
        return null;
    }
}
