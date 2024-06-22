package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ScriptContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class MemberAssignable implements Assignable {
    private final String memberName;
    private final Expression target;
    public MemberAssignable(Expression target, String memberName) {
        this.target = target;
        this.memberName = memberName;
    }

    @Override
    public Object assignValue(ScriptContext exec, Object value) throws TwistException {
        Object obj = target.evaluate(exec);

        if (obj == null) {
            throw new NullValueException(target.toString());
        }

        // option 1 - if obj is a Map instance, assign the value
        if (obj instanceof Map) {
            ((Map<String, Object> )obj).put(memberName, value);
            return value;
        }

        // option 2 - bean introspection
        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass(), Object.class, Introspector.IGNORE_ALL_BEANINFO);
            PropertyDescriptor descriptor = Arrays.stream(info.getPropertyDescriptors()).filter(pd -> pd.getName().equals(memberName)).findFirst().orElse(null);
            if (descriptor == null) {
                throw new TwistException("No method " + memberName + " on " + obj);
            }
            descriptor.getWriteMethod().invoke(obj, value);
            return value;
        } catch (IntrospectionException e) {
            throw new TwistException("Unable to find properties of " + obj, e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new TwistException("Unable to get properties of " + obj, e);
        }
    }
}
