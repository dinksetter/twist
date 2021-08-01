package com.inksetter.twist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class TwistValue implements Serializable, Comparable<TwistValue> {
    // Set up a double formatter that has no decimal point if it's not needed
    // and the same 15 precision that legacy supported (340 is the max here)
    // DecimalFormat's are not thread safe, so clone when necessary
    private static final DecimalFormat doubleFormatter = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ROOT));
    static { doubleFormatter.setMaximumFractionDigits(15); }
    
    public TwistValue(TwistDataType type, Object value) {
        _type = type;
        _value = value;
    }

    public TwistValue(Object value) {
        this(TwistDataType.forValue(value), value);
    }

    public static TwistValue NULL = new TwistValue(TwistDataType.STRING, null);
    
    public TwistDataType getType() {
        return _type;
    }
    
    public Object getValue() {
        return _value;
    }
    
    public boolean asBoolean() {
       
        // If the value is null, it becomes false.
        if (isNull()) return false;
        
        if (_type == TwistDataType.BOOLEAN) {
            return (Boolean) _value;
        }
        
        // If the value is an integer, zero == false, all else == true
        if (_type == TwistDataType.INTEGER) {
            return (Integer) _value != 0;
        }
        
        // If the value is an double, zero == false, all else == true
        if (_type == TwistDataType.DOUBLE) {
            return ((Number)_value).doubleValue() != 0.0;
        }
        
        // Otherwise, non-null equals true;
        return true;
    }
    
    public String asString() {
        if (_type == TwistDataType.STRING) {
            return (String)_value;
        }
        
        if (isNull()) {
            return null;
        }
        else {
            if (_type == TwistDataType.DATETIME) {
                return formatDate((Date)_value);
            }
            else if (_type == TwistDataType.BOOLEAN) {
                return (Boolean) _value ? "1" : "0";
            }
            else if (_type == TwistDataType.DOUBLE) {
                return ((DecimalFormat)doubleFormatter.clone()).format(_value);
            }
            else {
                return String.valueOf(_value);
            }
        }
    }
    
    public int asInt() {
        if (isNull()) return 0;
        
        if (_value instanceof Number) {
            return ((Number)_value).intValue();
        }
        
        if (_value instanceof String) {
            return Double.valueOf((String)_value).intValue();
        }
        
        if (_value instanceof Boolean) {
            return (Boolean)_value ? 1 : 0;
        }
        
        return 0;
    }
    
    public double asDouble() {
        if (isNull()) return 0.0;
        
        if (_value instanceof Number) {
            return ((Number)_value).doubleValue();
        }
        
        if (_value instanceof String) {
            return Double.parseDouble((String)_value);
        }
        
        if (_value instanceof Boolean) {
            return (Boolean)_value ? 1.0 : 0.0;
        }
        
        return 0.0;
    }
    
    public Date asDate() {
        if (isNull()) return null;
        
        if (_value instanceof Date) {
            return (Date)_value;
        }
        
        if (_value instanceof String) {
            return parseDate((String)_value);
        }
        
        return null; // TODO throw type cast exception
    }
    
    public boolean isNull() {
        // If the value is null or we have an empty string then we consider it
        // null.  Also we don't want to trim the string since any character
        // would represent this as not null.
        return _value == null ||
                (_value instanceof String && ((String) _value).isEmpty());
    }
    
    /**
     * Returns the value as an object of the passed type. If the type conversion
     * cannot occur, <code>null</code> is returned.  Otherwise, the standard type
     * conversion occurs.
     * @param type
     * @return
     */
    public Object asType(TwistDataType type) {
        if (type == _type || type == TwistDataType.OBJECT) {
            return _value;
        }
        
        else {
            switch (type) {
            case STRING:
                return asString();
            case BOOLEAN:
                return asBoolean();
            case DOUBLE:
                return asDouble();
            case INTEGER:
                return asInt();
            case DATETIME:
                return asDate();
            }
        }
        return null;
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_type == null) ? 0 : _type.hashCode());
        result = prime * result + ((_value == null) ? 0 : _value.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TwistValue other = (TwistValue) obj;
        
        if (this.isNull() && other.isNull()) {
            return true;
        }
        
        if (this._type == TwistDataType.STRING || other._type == TwistDataType.STRING) {
            return this.asSafeString().equals(other.asSafeString());
        }
        else if (this._type == TwistDataType.DOUBLE || other._type == TwistDataType.DOUBLE) {
            //This is to ensure precision when checking equality.
            return Double.doubleToLongBits(this.asDouble()) == Double
                .doubleToLongBits(other.asDouble());
        }        
        else if (this._type == TwistDataType.INTEGER || other._type == TwistDataType.INTEGER) {
            return this.asInt() == other.asInt();
        }
        else if (this._type == TwistDataType.BINARY && other._type == TwistDataType.BINARY) {
            return Arrays.equals((byte[])this._value, (byte[])other._value);
        }
        else {
            return this.asSafeString().equals(other.asSafeString());
        }
    }

    // @see java.lang.Comparable#compareTo(java.lang.Object)
    @Override
    public int compareTo(TwistValue right) {
        TwistValue left = this;

        if (left.isNull() && right.isNull()) {
            return 0;
        }
        
        if (left._type == TwistDataType.STRING || right._type == TwistDataType.STRING) {
            return left.asSafeString().compareTo(right.asSafeString());
        }
        else if (left._type == TwistDataType.DOUBLE || right._type == TwistDataType.DOUBLE) {
            double l = left.asDouble();
            double r = right.asDouble();
            return Double.compare(l, r);
        }
        else if (left._type == TwistDataType.INTEGER || right._type == TwistDataType.INTEGER) {
            return left.asInt() - right.asInt();
        }
        else {
            return left.asSafeString().compareTo(right.asSafeString());
        }
    }

    public static String formatDate(Date date) {
        if (date == null) return null;

        return DateTimeFormatter.ISO_DATE_TIME.format(date.toInstant());
    }

    public static Date parseDate(String dateString) {
        if (dateString == null) return null;

        return Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateString)));
    }



    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        return _value + "(" +_type + ")";
    }
    
    //
    // Implementation
    //
    
    private String asSafeString() {
        if (isNull()) {
            return "";
        }
        else {
            return asString();
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        
        if (_value instanceof Serializable || _value instanceof Externalizable) {
            out.writeObject(_value);
        }
        else {
            out.writeObject(null);
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        _value = in.readObject();
    }
    
    private static final long serialVersionUID = -732742411090773154L;
    private final TwistDataType _type;
    private transient Object _value;
}
