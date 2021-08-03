package com.inksetter.twist.expression.function;

import java.util.Base64;
import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

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
