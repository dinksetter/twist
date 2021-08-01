package com.inksetter.twist.expression.function;

import java.util.Base64;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Decodes a base64 string into a binary data item (byte array).
 */
public class Base64DecodeFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        TwistValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new TwistValue(TwistDataType.BINARY, null);
        }

        if (argValue.getType() != TwistDataType.STRING) {
            throw new FunctionArgumentException("expected String argument");
        }


        return new TwistValue(TwistDataType.BINARY, Base64.getDecoder().decode(argValue.asString()));
    }
}
