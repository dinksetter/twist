package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class MemberExpression implements Assignable {

    public MemberExpression(Expression target, String memberName) {
        _target = target;
        _memberName = memberName;
    }

    @Override
    public Object evaluate(EvalContext ctx) throws TwistException {
        Object obj = _target.evaluate(ctx);
        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        // option 1 - if obj is a Map instance, look for members.
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(_memberName);
        }

        // option 2 - bean introspection
        Class<?> cls = obj.getClass();
        String methodSuffix = Character.toUpperCase(_memberName.charAt(0)) + _memberName.substring(1);
        Method getter;
        try {
            try {
                getter = cls.getMethod("get" + methodSuffix);
            }
            catch (NoSuchMethodException e) {
                getter = cls.getMethod("is" + methodSuffix);
            }
            return getter.invoke(obj);
        } catch (NoSuchMethodException e) {
            throw new TwistException("Unable to find properties of " + obj, e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new TwistException("Unable to get properties of " + obj, e);
        }
    }

    @Override
    public void assignValue(EvalContext exec, Object value) throws TwistException {
        Object obj = _target.evaluate(exec);

        if (obj == null) {
            throw new NullValueException(_target.toString());
        }

        // option 1 - if obj is a Map instance, assign the value
        if (obj instanceof Map) {
            ((Map<String, Object> )obj).put(_memberName, value);
            return;
        }

        // option 2 - bean introspection
        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass(), Object.class, Introspector.IGNORE_ALL_BEANINFO);
            PropertyDescriptor descriptor = Arrays.stream(info.getPropertyDescriptors()).filter(pd -> pd.getName().equals(_memberName)).findFirst().orElse(null);
            if (descriptor == null) {
                throw new TwistException("No method " + _memberName + " on " + obj);
            }
            descriptor.getWriteMethod().invoke(obj, value);
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
