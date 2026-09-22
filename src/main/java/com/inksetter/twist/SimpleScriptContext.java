package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.*;

/**
 * The standard {@link ScriptContext}. Each instance is one stack frame, holding its own variables
 * and a reference to the frame that encloses it. Functions are registered once, on the root frame,
 * and shared by every frame below it.
 */
public class SimpleScriptContext implements ScriptContext {

    private final Map<String, Object> vars;
    private final SimpleScriptContext parent;
    private final boolean barrier;
    private final Map<String, TwistFunction> functions;

    public SimpleScriptContext() {
        this(Map.of(), Map.of());
    }

    public SimpleScriptContext(Map<String,Object> initial, Map<String, TwistFunction> functions) {
        this.vars = new LinkedHashMap<>(initial);
        this.parent = null;
        this.barrier = false;
        this.functions = new HashMap<>(functions);
    }

    private SimpleScriptContext(SimpleScriptContext parent, boolean barrier) {
        this.vars = new LinkedHashMap<>();
        this.parent = parent;
        this.barrier = barrier;
        this.functions = parent.functions;
    }

    @Override
    public ScriptContext push() {
        return new SimpleScriptContext(this, false);
    }

    @Override
    public ScriptContext pushCall() {
        return new SimpleScriptContext(this, true);
    }

    @Override
    public boolean isDefined(String name) {
        for (SimpleScriptContext frame = this; frame != null; frame = frame.parent) {
            if (frame.vars.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getVariable(String name) {
        // Reads see the whole chain: this frame, then the frames enclosing it.
        for (SimpleScriptContext frame = this; frame != null; frame = frame.parent) {
            if (frame.vars.containsKey(name)) {
                return frame.vars.get(name);
            }
        }
        return null;
    }

    @Override
    public void setVariable(String name, Object value) {
        // Assigning to an existing name updates it where it lives, unless a call boundary is in the
        // way. Otherwise the variable is created in this frame.
        for (SimpleScriptContext frame = this; frame != null; frame = frame.parent) {
            if (frame.vars.containsKey(name)) {
                frame.vars.put(name, value);
                return;
            }
            if (frame.barrier) {
                break;
            }
        }

        vars.put(name, value);
    }

    @Override
    public void defineLocal(String name, Object value) {
        vars.put(name, value);
    }

    @Override
    public Map<String, Object> getAll() {
        // Everything visible for assignment: this frame and its enclosing frames, up to and
        // including the first call boundary. Inner frames shadow outer ones.
        Deque<SimpleScriptContext> frames = new ArrayDeque<>();
        for (SimpleScriptContext frame = this; frame != null; frame = frame.parent) {
            frames.addFirst(frame);
            if (frame.barrier) {
                break;
            }
        }

        Map<String,Object> all = new LinkedHashMap<>();
        for (SimpleScriptContext frame : frames) {
            all.putAll(frame.vars);
        }
        return all;
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
