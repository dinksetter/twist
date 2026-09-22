package com.inksetter.twist.expression.function;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.ExceptionMatcher;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.expression.ReferenceExpression;

import java.util.List;

/**
 * Runs a block of script and checks that it throws a given type of exception, as in
 * <code>assertThrows(NumberFormatException, -&gt; { int('x') })</code>. The block can also be a bare
 * expression: <code>assertThrows(NumberFormatException, int('x'))</code>.
 * <p>
 * The exception type is matched the same way a catch block matches it: by simple class name,
 * including supertypes, against the exception and the causes it wraps. The matching exception is
 * returned, so a script can go on to check its message.
 * <p>
 * This function isn't a built-in. Register it where it's wanted:
 * <code>context.addFunction("assertThrows", new AssertThrowsFunction())</code>.
 */
public class AssertThrowsFunction implements ExpressionFunction {

    @Override
    public Object invokeRaw(List<Expression> args, EvalContext ctx) throws TwistException {
        if (args.size() != 2) {
            throw new FunctionArgumentException("expected 2 arguments");
        }

        String typeName = typeName(args.get(0), ctx);

        try {
            // A lambda evaluates to a function value, which we then call. Any other expression is
            // simply evaluated here, inside the try.
            Object block = args.get(1).evaluate(ctx);
            if (block instanceof TwistFunction) {
                ((TwistFunction) block).invoke(List.of(), ctx);
            }
        }
        catch (Exception e) {
            Throwable matched = ExceptionMatcher.match(typeName, e);
            if (matched != null) {
                return matched;
            }
            throw new TwistException("expected " + typeName + ", but threw " + e, e);
        }

        throw new TwistException("expected " + typeName + ", but nothing was thrown");
    }

    /**
     * Resolves the exception type, which can be written as a bare type name, a string, or an
     * expression that evaluates to either of those or to a Class.
     */
    private String typeName(Expression typeExpr, EvalContext ctx) throws TwistException {
        if (typeExpr instanceof ReferenceExpression) {
            String name = ((ReferenceExpression) typeExpr).getName();
            if (!ctx.isDefined(name)) {
                return name;
            }
        }

        Object typeValue = typeExpr.evaluate(ctx);
        if (typeValue instanceof Class) {
            return ((Class<?>) typeValue).getSimpleName();
        }
        if (typeValue instanceof String) {
            return (String) typeValue;
        }

        throw new FunctionArgumentException("expected an exception type, got " + typeValue);
    }
}
