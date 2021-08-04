package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodCallExpression implements Expression {

    public MethodCallExpression(Expression target, String memberName, List<Expression> methodArgs) {
        _target = target;
        _memberName = memberName;
        _methodArgs = methodArgs;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        Object obj = _target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        List<Object> list = new ArrayList<>();
        for (Expression a : _methodArgs) {
            Object evaluate = a.evaluate(ctx);
            list.add(evaluate);
        }
        Object[] argValues = list.toArray();

        try {

            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            for (MethodDescriptor desc : info.getMethodDescriptors()) {
                String methodName = desc.getName();

                if (_memberName.equals(desc.getName())) {
                    Method method = desc.getMethod();
                    if (method.getParameterCount() == _methodArgs.size()) {
                        return method.invoke(obj, argValues);
                    }
                }
            }
            throw new UnrecognizedMethodException(_memberName);
        } catch (IntrospectionException e) {
            throw new TwistException("Unable to find properties of " + obj, e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new TwistException("Unable to get properties of " + obj, e);
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "." + _memberName;
    }

    private final Expression _target;
    private final String _memberName;
    private final List<Expression> _methodArgs;
}
