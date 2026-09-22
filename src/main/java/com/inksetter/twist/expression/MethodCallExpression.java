package com.inksetter.twist.expression;

import com.inksetter.twist.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodCallExpression implements Expression {

    public MethodCallExpression(Expression target, String methodName, List<Expression> methodArgs) {
        this.target = target;
        this.methodName = methodName;
        this.methodArgs = methodArgs;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object obj = target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(target.toString());
        }

        Object[] argValues = new Object[methodArgs.size()];
        for (int i = 0; i < methodArgs.size(); i++) {
            Expression a = methodArgs.get(i);
            argValues[i] = a.evaluate(ctx);
        }

        if (obj instanceof MethodInterceptor) {
            return ((MethodInterceptor)obj).invokeMethod(methodName, argValues);
        }

        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            for (MethodDescriptor desc : info.getMethodDescriptors()) {

                if (methodName.equals(desc.getName())) {
                    Method method = desc.getMethod();
                    if (method.getParameterCount() == argValues.length) {
                        Class<?>[] types = method.getParameterTypes();
                        boolean matching = true;
                        for (int i = 0; i < types.length && matching; i++) {
                            if (argValues[i] != null && !ValueUtils.isCompatible(argValues[i].getClass(), types[i])) {
                                matching = false;
                            }
                        }
                        if (matching) {
                            try {
                                return method.invoke(obj, argValues);
                            } catch (IllegalArgumentException e) {
                                throw new TwistException("Unable to call method " + methodName + ":" + e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            throw new UnrecognizedMethodException(methodName);
        } catch (IntrospectionException e) {
            throw new TwistException("Unable to find methods of " + obj, e);
        } catch (InvocationTargetException e) {
            // Keep the exception the method actually threw as the cause, so that catch blocks
            // can match on its type.
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new TwistException(methodName + "() threw " + cause, cause);
        } catch (IllegalAccessException e) {
            throw new TwistException("Unable to call method " + methodName + " on " + obj, e);
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return target + "." + methodName;
    }

    private final Expression target;
    private final String methodName;
    private final List<Expression> methodArgs;
}
