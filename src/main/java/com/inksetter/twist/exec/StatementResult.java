package com.inksetter.twist.exec;

public class StatementResult {
    private final Type type;
    private final Object value;
    public enum Type {
        NORMAL, RETURN, BREAK
    }

    private StatementResult(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static StatementResult valueResult(Object value) {
        return new StatementResult(Type.NORMAL, value);
    }

    public static StatementResult returnResult(Object value) {
        return new StatementResult(Type.RETURN, value);
    }

    public static StatementResult breakResult() {
        return new StatementResult(Type.BREAK, null);
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
