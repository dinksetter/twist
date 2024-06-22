package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.EvalContext;

import java.util.List;

/**
 * Calls the equivalent of the C sprintf function.  This method actually
 * uses the Java <code>String.format</code> method. 
 */
public class SprintfFunction extends BaseFunction {

    @Override
    protected String invoke(EvalContext ctx, List<Object> args) throws TwistException {
        if (args.size() < 1) {
            throw new FunctionArgumentException("expected format argument");
        }
        
        String format = ValueUtils.asString(args.get(0));
        
        Object[] sprintfArgs = new Object[args.size() - 1];
        
        for (int i = 0; i < sprintfArgs.length; i++) {
            sprintfArgs[i] = args.get(i + 1);
        }
        
        return String.format(format, sprintfArgs);
    }
}
