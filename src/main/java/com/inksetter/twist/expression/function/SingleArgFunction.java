package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;
import com.inksetter.twist.expression.Expression;

import java.util.List;

/**
 * Decodes a base64 string into a binary data item (byte array).
 */
public abstract class SingleArgFunction extends BaseFunction {

    @Override
    protected final void validateArgs(List<Expression> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
    }

    @Override
    protected final Object invoke(SymbolSource ctx, List<Object> args) throws TwistException {
        return this.invoke(args.get(0));
    }

    protected abstract Object invoke(Object argValue) throws TwistException;
}
