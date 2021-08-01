package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Returns a new string that is a substring of the string argument.
 */
public class SubstrFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() < 2 || args.size() > 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
        
        String format = args.get(0).asString();
        int start = args.get(1).asInt();
        
        
        if (format == null) {
            return new TwistValue(TwistDataType.STRING, null);
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
            int length = args.get(2).asInt();
            
            if (length <= 0) {
                return new TwistValue(TwistDataType.STRING, "");
            }
            
            if ((length + start) > format.length()) {
                length = format.length() - start;
            }
            
            return new TwistValue(TwistDataType.STRING, format.substring(start, length + start));
        }
        else {
            return new TwistValue(TwistDataType.STRING, format.substring(start));
        }
    }
}
