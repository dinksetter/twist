package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.TwistException;

import java.util.List;

/**
 * Decodes a base64 string into a binary data item (byte array).
 */
public abstract class SingleArgFunction extends BaseFunction {
    @Override
    public final Object invoke(List<Object> args, EvalContext context) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("unexpected arguments: " + args);
        }

        return this.invoke(args.get(0));
    }

    protected abstract Object invoke(Object argValue) throws TwistException;
}
