package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.Expression;

import java.util.List;

/**
 * Returns the index of a string within another string.  -1 indicates that
 * the search string was not found in the source string.
 */
public class IndexOfFunction extends BaseFunction {

    @Override
    public Integer invoke(List<Object> args) throws TwistException {
        if (args.size() != 2 && args.size() != 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }

        String source = ValueUtils.asString(args.get(0));
        String search = ValueUtils.asString(args.get(1));
        int start = 0;
        
        if (args.size() == 3) {
            start = ValueUtils.asInt(args.get(2));
        }
        
        return source.indexOf(search, start) + 1;
    }
}
