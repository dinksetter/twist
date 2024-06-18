package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectExpression implements Expression {
    private final Map<String, Expression> objData;
    public ObjectExpression(Map<String, Expression> mapping) {
        objData = mapping;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, Expression> entry : objData.entrySet()) {
            data.put(entry.getKey(), entry.getValue().evaluate(ctx));
        }
        return data;
    }
}
