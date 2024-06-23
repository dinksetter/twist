package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.EvalContext;
import com.inksetter.twist.expression.Expression;

import java.util.List;

/**
 * Decodes a base64 string into a binary data item (byte array).
 */
public abstract class SingleArgFunction extends BaseFunction {

    @Override
    public final void validateArgs(List<Expression> args) throws FunctionArgumentException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
    }

    @Override
    protected final Object invoke(EvalContext ctx, List<Object> args) throws TwistException {
        return this.invoke(args.get(0));
    }

    protected abstract Object invoke(Object argValue) throws TwistException;
}
