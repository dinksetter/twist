package com.inksetter.twist.expression.function;

import java.util.List;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

/**
 * Calls the equivalent of the C sprintf function.  This method actually
 * uses the Java <code>String.format</code> method. 
 */
public class SprintfFunction extends BaseFunction {

    @Override
    protected TwistValue invoke(ExecContext ctx, List<TwistValue> args) throws TwistException {
        if (args.size() < 1) {
            throw new FunctionArgumentException("expected format argument");
        }
        
        String format = args.get(0).asString();
        
        Object[] sprintfArgs = new Object[args.size() - 1];
        
        for (int i = 0; i < sprintfArgs.length; i++) {
            sprintfArgs[i] = args.get(i + 1).getValue();
        }
        
        return new TwistValue(TwistDataType.STRING, String.format(format, sprintfArgs));
    }
}
