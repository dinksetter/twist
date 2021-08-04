package com.inksetter.twist.expression.function;

import com.inksetter.twist.ValueUtils;

import java.util.Base64;

/**
 * Decodes a base64 string into a binary data item (byte array).
 */
public class Base64DecodeFunction extends SingleArgFunction {

    @Override
    protected byte[] invoke(Object argValue) {
        if (ValueUtils.isNull(argValue)) {
            return null;
        }

        return Base64.getDecoder().decode(argValue.toString());
    }
}
