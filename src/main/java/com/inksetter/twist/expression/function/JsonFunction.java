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
            out.append('"').append(value).append('"');
        }
        else if (value instanceof Number) {
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
                out.append("\"");
                out.append(entry.getKey());
                out.append("\":");
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
            out.append('"').append(ValueUtils.asString(value)).append('"');
        }
        return out;
    }

}
