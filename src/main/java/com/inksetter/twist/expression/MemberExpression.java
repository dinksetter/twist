package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class MemberExpression implements Expression {

    public MemberExpression(Expression target, String memberName) {
        _target = target;
        _memberName = memberName;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
        Object obj = _target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        // option 1 - if obj is a Map instance, look for members.
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(_memberName);
        }

        // option 2 - bean introspection
        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass(), Object.class, Introspector.IGNORE_ALL_BEANINFO);
            PropertyDescriptor descriptor = Arrays.stream(info.getPropertyDescriptors()).filter(pd -> pd.getName().equals(_memberName)).findFirst().orElse(null);
            if (descriptor == null) {
                throw new TwistException("No method " + _memberName + " on " + obj);
            }
            return descriptor.getReadMethod().invoke(obj);
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
}
