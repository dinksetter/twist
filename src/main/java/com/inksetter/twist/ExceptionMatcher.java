package com.inksetter.twist;

import java.util.ArrayList;
import java.util.List;

/**
 * Matches thrown exceptions against the type names used in catch blocks. Exceptions thrown by Java
 * code are wrapped in a {@link TwistException}, so a type name can match either the wrapper or the
 * exception that caused it.
 */
public final class ExceptionMatcher {

    private ExceptionMatcher() {
    }

    /**
     * Returns the exception and the causes it wraps, outermost first. The chain stops at the first
     * exception that isn't a TwistException, since anything below that is the original exception's
     * own business.
     */
    public static List<Throwable> causeChain(Throwable thrown) {
        List<Throwable> chain = new ArrayList<>();
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            chain.add(t);
            if (!(t instanceof TwistException)) {
                break;
            }
        }
        return chain;
    }

    /**
     * Returns the exception in the cause chain whose type, or one of its supertypes, has the given
     * simple class name, or null if none of them does.
     */
    public static Throwable match(String typeName, Throwable thrown) {
        List<Throwable> candidates = causeChain(thrown);

        // Deepest cause first, so that a general type name like Exception matches the exception that
        // was originally thrown rather than the TwistException wrapping it.
        for (int i = candidates.size() - 1; i >= 0; i--) {
            Throwable caught = candidates.get(i);
            for (Class<?> cls = caught.getClass(); cls != null; cls = cls.getSuperclass()) {
                if (cls.getSimpleName().equals(typeName)) {
                    return caught;
                }
            }
        }
        return null;
    }
}
