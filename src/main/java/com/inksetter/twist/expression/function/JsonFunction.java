package com.inksetter.twist.expression.function;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.Map;

/**
 * Casts the argument to a string.
 */
public class JsonFunction extends SingleArgFunction {

    @Override
    protected Object invoke(Object argValue) throws TwistException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(argValue.toString(), Map.class);
        } catch (JsonProcessingException e) {
            throw new TwistException("invalid json: " + argValue, e);
        }
    }
}
