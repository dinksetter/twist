package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.*;

public class SimpleScriptContext implements ScriptContext {

    private final Deque<Map<String, Object>> varStack = new LinkedList<>();
    private final Map<String, TwistFunction> functions = new HashMap<>();
    private final Map<String, Object> baseVars = new LinkedHashMap<>();

    public SimpleScriptContext() {
        varStack.addFirst(baseVars);
    }

    public SimpleScriptContext(Map<String,Object> initial, Map<String, TwistFunction> functions) {
        baseVars.putAll(initial);
        varStack.addFirst(baseVars);
        this.functions.putAll(functions);
    }

    @Override
    public void popStack() {
        varStack.removeFirst();
        if (!varStack.isEmpty() && varStack.peekFirst() == null) {
            varStack.removeFirst();
        }
    }

    @Override
    public void pushStack(boolean fresh) {
        if (fresh) {
            varStack.addFirst(null);
        }

        varStack.addFirst(new LinkedHashMap<>());
    }

    @Override
    public boolean isDefined(String name) {
        for (Map<String, Object> symbols : varStack) {
            if (symbols == null) {
                return baseVars.containsKey(name);
            }
            if (symbols.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getVariable(String name) {
        for (Map<String, Object> symbols : varStack) {
            if (symbols == null) {
                return baseVars.get(name);
            }
            if (symbols.containsKey(name)) {
                return symbols.get(name);
            }
        }
        return null;
    }

    @Override
    public void setVariable(String name, Object value) {
        for (Map<String, Object> symbols : varStack) {
            if (symbols == null) {
                break;
            }
            else if (symbols.containsKey(name)) {
                symbols.put(name, value);
                return;
            }
        }

        varStack.getFirst().put(name, value);
    }

    @Override
    public Map<String, Object> getAll() {
        Map<String,Object> vars = new LinkedHashMap<>();
        for (Map<String, Object> symbols : varStack) {
            if (symbols == null) {
                break;
            }

            vars.putAll(symbols);
        }
        return vars;
    }

    @Override
    public TwistFunction lookupFunction(String name) {
        return functions.get(name);
    }

    @Override
    public void addFunction(String name, TwistFunction function) {
        functions.put(name, function);
    }

    public List<String> getFunctionNames() {
        return new ArrayList<>(functions.keySet());
    }

}
