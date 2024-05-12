package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodCallExpression implements Expression {

    public MethodCallExpression(Expression target, String methodName, List<Expression> methodArgs) {
        _target = target;
        _methodName = methodName;
        _methodArgs = methodArgs;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        Object obj = _target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        Object[] argValues = new Object[_methodArgs.size()];
        for (int i = 0; i < _methodArgs.size(); i++) {
            Expression a = _methodArgs.get(i);
            argValues[i] = a.evaluate(ctx);
        }

        // First check for common patterns
        // 1. list/collection type built-in overrides
        //   - filter(

        try {

            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            for (MethodDescriptor desc : info.getMethodDescriptors()) {
                String methodName = desc.getName();

                if (_methodName.equals(desc.getName())) {
                    Method method = desc.getMethod();
                    if (method.getParameterCount() == argValues.length) {
                        try {
                            return method.invoke(obj, argValues);
                        }
                        catch (IllegalArgumentException e) {
                            throw new TwistException("Unable to call method " + _methodName + ":" + e.getMessage(), e);
                        }
                    }
                }
            }

            throw new UnrecognizedMethodException(_methodName);
        } catch (IntrospectionException e) {
            throw new TwistException("Unable to find properties of " + obj, e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new TwistException("Unable to get properties of " + obj, e);
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "." + _methodName;
    }

    private final Expression _target;
    private final String _methodName;
    private final List<Expression> _methodArgs;
}
