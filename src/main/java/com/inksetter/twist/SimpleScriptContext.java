package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.expression.function.TwistFunction;

import java.util.*;

/**
 * The standard {@link ScriptContext}. Each instance is one stack frame, holding its own variables
 * and a reference to the frame that encloses it. Functions are registered once, on the root frame,
 * and shared by every frame below it.
 * <p>
 * A host can subclass this to add behavior, typically a fallback for names the context doesn't
 * know. Nested frames — the ones created for blocks, loops, function bodies and lambdas — are plain
 * frames, but they delegate to the root frame, which is the instance the host created. So
 * overriding {@link #getVariable}, {@link #isDefined}, {@link #lookupFunction} or
 * {@link #addFunction} takes effect everywhere in a script, at any depth.
 * <p>
 * Overriding {@link #setVariable} only intercepts writes made at the top level of a script, since a
 * variable created in a nested scope belongs to that frame.
 */
public class SimpleScriptContext implements ScriptContext {

    private final Map<String, Object> vars;
    private final SimpleScriptContext parent;
    private final SimpleScriptContext root;
    private final boolean barrier;
    private final Map<String, TwistFunction> functions;

    public SimpleScriptContext() {
        this(Map.of(), Map.of());
    }

    public SimpleScriptContext(Map<String,Object> initial, Map<String, TwistFunction> functions) {
        this.vars = new LinkedHashMap<>(initial);
        this.parent = null;
        this.root = this;
        this.barrier = false;
        this.functions = new HashMap<>(functions);
    }

    private SimpleScriptContext(SimpleScriptContext parent, boolean barrier) {
        this.vars = new LinkedHashMap<>();
        this.parent = parent;
        this.root = parent.root;
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
        if (vars.containsKey(name)) {
            return true;
        }
        // Delegating rather than walking the chain here, so that an override on the root frame
        // applies no matter which frame the question was asked in.
        return parent != null && parent.isDefined(name);
    }

    @Override
    public Object getVariable(String name) {
        // Reads see the whole chain: this frame, then the frames enclosing it.
        if (vars.containsKey(name)) {
            return vars.get(name);
        }
        return parent == null ? null : parent.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        // Assigning to an existing name updates it where it lives, unless a call boundary is in the
        // way. Otherwise the variable is created in this frame.
        if (!assignToExisting(name, value)) {
            vars.put(name, value);
        }
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
        if (root != this) {
            return root.lookupFunction(name);
        }
        return functions.get(name);
    }

    @Override
    public void addFunction(String name, TwistFunction function) {
        if (root != this) {
            root.addFunction(name, function);
            return;
        }
        functions.put(name, function);
    }

    public List<String> getFunctionNames() {
        if (root != this) {
            return root.getFunctionNames();
        }
        return new ArrayList<>(functions.keySet());
    }

    /**
     * Assigns to an existing binding in this frame or an enclosing one, stopping at a call
     * boundary. Returns false if the name isn't bound anywhere that this frame can assign to.
     */
    private boolean assignToExisting(String name, Object value) {
        if (vars.containsKey(name)) {
            vars.put(name, value);
            return true;
        }
        if (barrier || parent == null) {
            return false;
        }
        return parent.assignToExisting(name, value);
    }
}
