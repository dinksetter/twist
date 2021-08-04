package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.Base64;

/**
 * Encodes a binary field (byte array) into a base-64 encoded string value.
 */
public class Base64EncodeFunction extends SingleArgFunction {

    @Override
    protected String invoke(Object argValue) throws TwistException {
        if (ValueUtils.isNull(argValue)) {
            return null;
        }

        if (ValueUtils.getType(argValue) != TwistDataType.BINARY) {
            throw new FunctionArgumentException("expected binary argument");
        }

        return Base64.getEncoder().encodeToString((byte[])argValue);
    }
}
