package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class MemberExpression implements Expression {

    public MemberExpression(Expression target, String memberName) {
        _target = target;
        _memberName = memberName;
    }

    @Override
    public Object evaluate(SymbolSource ctx) throws TwistException {
        Object obj = _target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        // option 1 - if obj is a Map instance, look for members.
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(_memberName);
        }

        // option 2 - getter
        try {
            Class<?> cls = obj.getClass();
            Method readMethod;
            try {
                readMethod = cls.getMethod("get" + Character.toUpperCase(_memberName.charAt(0)) + _memberName.substring(1));
            } catch (NoSuchMethodException e) {
                readMethod = cls.getMethod("is" + Character.toUpperCase(_memberName.charAt(0)) + _memberName.substring(1));
            }

            return readMethod.invoke(obj);
        } catch (NoSuchMethodException e) {
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
}
