package com.inksetter.twist.expression.function;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.expression.Expression;

import java.util.List;

/**
 * The basic built-in function definition.  Functions are handed a list of expressions as arguments, and must 
 * evaluate them based on the needs of the specific function.  Most functions will extend the abstract class
 * <code>BaseFunction</code> to facilitate the evaluation of function arguments.
 */
public interface TwistFunction {
    
    /**
     * Call this function. When this method is called, all arguments will have
     * been evaluated. The value of those arguments are passed into the
     * <code>invoke</code> method as the <code>args</code> parameter.
     * 
     * @param ctx The <code>ServerContext</code> to use for execution of the
     *            function, if it needs to get data or execute commands within
     *            the server execution context.
     * @param args Arguments to the function call.
     * @return a single value.
     * @throws TwistException if an error occurs.
     */
    Object evaluate(ExecContext ctx, List<Expression> args) throws TwistException;
}
