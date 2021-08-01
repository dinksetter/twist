package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns the index of a string within another string.  -1 indicates that
 * the search string was not found in the source string.
 */
public class IndexOfFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() != 2 && args.size() != 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
        
        String source = args.get(0).asString();
        String search = args.get(1).asString();
        int start = 0;
        
        if (args.size() == 3) {
            start = args.get(2).asInt();
        }
        
        return new TwistValue(TwistDataType.INTEGER, source.indexOf(search, start) + 1);
    }
}
