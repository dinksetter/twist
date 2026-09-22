package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonFunction implements TwistFunction {
    @Override
    public Object invoke(List<Object> args, EvalContext context) throws TwistException {
        if (args.isEmpty() || args.size() > 2) {
            throw new FunctionArgumentException("unexpected arguments: " + args);
        }

        Object value = args.get(0);
        boolean pretty = (args.size() > 1) ? ValueUtils.asBoolean(args.get(1)) : false;

        return render2(args.get(0), 0, pretty).toString();
    }

    private CharSequence render2(Object value, int indent, boolean pretty) {
        StringBuilder out = new StringBuilder();
        if (value == null) {
            out.append("null");
        }
        else if (value instanceof String) {
            appendQuoted(out, (String) value);
        }
        else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        }
        else if (value instanceof List<?>) {
            out.append('[');
            if (pretty) out.append('\n');
            String prefix = "  ".repeat(indent + 1);
            List<?> list = ((List<?>) value);
            for (Iterator<?> i = list.iterator(); i.hasNext(); ) {
                Object entry = i.next();
                if (pretty) out.append(prefix);
                out.append(render2(entry, indent + 1, pretty));
                if (i.hasNext()) {
                    out.append(',');
                    if (pretty) out.append('\n');
                }
            }
            if (pretty) {
                out.append('\n');
                out.append("  ".repeat(indent));
            }
            out.append("]");
        }
        else if (value instanceof Map<?,?>) {
            Map<?,?> map = (Map<?,?>)value;
            out.append('{');
            if (pretty) out.append('\n');
            String prefix = "  ".repeat(indent + 1);
            for (Iterator<? extends Map.Entry<?, ?>> i = map.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<?,?> entry = i.next();
                if (pretty) out.append(prefix);
                appendQuoted(out, ValueUtils.asString(entry.getKey()));
                out.append(":");
                out.append(render2(entry.getValue(), indent + 1, pretty));
                if (i.hasNext()) {
                    out.append(',');
                    if (pretty) out.append('\n');
                }
            }
            if (pretty) {
                out.append('\n');
                out.append("  ".repeat(indent));
            }
            out.append("}");
        }
        else {
            appendQuoted(out, ValueUtils.asString(value));
        }
        return out;
    }

    private void appendQuoted(StringBuilder out, String value) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }
}
