package com.inksetter.twist;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Types of data supported in Twist values
 */
public enum TwistDataType
{
    // Order is important, for type promotion.
    // BOOLEAN < INTEGER < DOUBLE < DATETIME < STRING
    // All others are not significant.
    BOOLEAN(Boolean.class),
    INTEGER(Integer.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    DATETIME(Date.class),
    STRING(String.class),
    BINARY(byte[].class),
    ARRAY(List.class),
    OBJECT(Object.class),
    UNKNOWN(Object.class);

    /**
     * Returns the Java class that represents this type.
     * @return the Java class that represents this type.
     */
    public Class<?> getValueClass() {
        return _valueClass;
    }
    
    /**
     * Determine what data type is appropriate for the given class.  This
     * method will never return reference types, as they are indistinguishable
     * from their value counterparts.
     * @param cls the class to be used to look up a type object.
     * @return the <code>TwistDataType</code> object corresponding to the given
     * class.  If there is no known type for <code>cls</code>,
     * <code>UNKNOWN</code> is returned.
     */
    public static TwistDataType lookupClass(Class<?> cls) {
        if (cls == String.class)
            return STRING;
        else if (cls == Long.class || cls == Long.TYPE)
            return LONG;
        else if (cls == Integer.class || cls == Integer.TYPE)
            return INTEGER;
        else if (cls == Double.class || cls == Double.TYPE)
            return DOUBLE;
        else if (cls == Boolean.class || cls == Boolean.TYPE)
            return BOOLEAN;
        else if (Date.class.isAssignableFrom(cls))
            return DATETIME;
        else if (Number.class.isAssignableFrom(cls))
            return DOUBLE;
        else if (cls.isArray() && cls.getComponentType() == Byte.TYPE)
            return BINARY;
        else if (cls.isArray() || cls.isAssignableFrom(Collection.class))
            return ARRAY;
        else if (!cls.isPrimitive())
            return OBJECT;
        else
            return UNKNOWN;
    }
    
    public static TwistDataType forValue(Object value) {
        if (value == null) {
            return TwistDataType.STRING;
        }
        else {
            return lookupClass(value.getClass());
        }
    }
    
    //
    // Implementation
    //
    TwistDataType(Class<?> cls) {
        _valueClass = cls;
    }
    
    private static final long serialVersionUID = 3834305120656962609L;
    private final transient Class<?> _valueClass;
}
