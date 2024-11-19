package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class JsonFunction extends SingleArgFunction {
    @Override
    protected Object invoke(Object argValue) throws TwistException {
        return render(argValue).toString();
    }

    private CharSequence render(Object value) {
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
            out.append(((List)value).stream().map(this::render).collect(Collectors.joining(",","[", "]")));
        }
        else if (value instanceof Map<?,?>) {
            Map<?,?> map = (Map<?,?>)value;
            out.append(map.entrySet().stream()
                    .map(entry ->"\"" + entry.getKey() + "\":" + render(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}")));
        }
        else {
            out.append('"').append(ValueUtils.asString(value)).append('"');
        }
        return out;
    }

}
