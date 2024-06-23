package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.EvalContext;
import com.inksetter.twist.expression.Expression;

import java.util.List;

/**
 * Returns a new string that is a substring of the string argument.
 */
public class SubstrFunction extends BaseFunction {

    @Override
    protected String invoke(EvalContext ctx, List<Object> args) {
        String format = ValueUtils.asString(args.get(0));
        int start = ValueUtils.asInt(args.get(1));

        if (format == null) {
            return null;
        }
            
        // Convert to 0-based index with the following rules:
        // - 0 (1-based) is the same as 1 (1-based)
        // - Negative numbers count from the end of the string
        // - Numbers that are too large (pos or neg) are just set to end
        if (Math.abs(start) > format.length()) {
            start = format.length();
        }
        else if (start > 0) {
            start--;
        }
        else if (start < 0) {
            start = format.length() + start;
        }
        
        if (args.size() > 2) {
            int length = ValueUtils.asInt(args.get(2));
            
            if (length <= 0) {
                return "";
            }
            
            if ((length + start) > format.length()) {
                length = format.length() - start;
            }
            
            return format.substring(start, length + start);
        }
        else {
            return format.substring(start);
        }
    }

    @Override
    public void validateArgs(List<Expression> args) throws FunctionArgumentException {
        if (args.size() < 2 || args.size() > 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
    }
}
