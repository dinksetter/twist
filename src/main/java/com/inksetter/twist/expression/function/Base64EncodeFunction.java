package com.inksetter.twist.expression.function;

import java.util.Base64;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Encodes a binary field (byte array) into a base-64 encoded string value.
 */
public class Base64EncodeFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        TwistValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new TwistValue(TwistDataType.STRING, null);
        }

        if (argValue.getType() != TwistDataType.BINARY) {
            throw new FunctionArgumentException("expected binary argument");
        }

        return new TwistValue(TwistDataType.STRING, Base64.getEncoder().encodeToString((byte[])argValue.getValue()));
    }
}
