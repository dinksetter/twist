package com.inksetter.twist;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class ValueUtils {
    // Set up a double formatter that has no decimal point if it's not needed
    // and the same 15 precision that legacy supported (340 is the max here)
    // DecimalFormat's are not thread safe, so clone when necessary
    private static final DecimalFormat doubleFormatter = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ROOT));
    static { doubleFormatter.setMaximumFractionDigits(15); }

    public static TwistDataType getType(Object value) {
        return TwistDataType.forValue(value);
    }
    
    public static boolean asBoolean(Object value) {
       
        // If the value is null, it becomes false.
        if (value == null) return false;

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Double) {
            return ((Double)value != 0.0);
        }

        if (value instanceof Number) {
            return ((Number)value).longValue() != 0L;
        }

        if (value instanceof String) {
            return ((String)value).isEmpty();
        }
        
        // Otherwise, non-null equals true;
        return true;
    }
    
    public static String asString(Object value) {

        if (value == null || value instanceof String) {
            return (String)value;
        }

        if (value instanceof Date) {
            return formatDate((Date)value);
        }

        if (value instanceof Double) {
            return ((DecimalFormat)doubleFormatter.clone()).format(value);
        }

        return value.toString();
    }
    
    public static long asLong(Object value) {
        if (isNull(value)) return 0;

        if (value instanceof Number) {
            return ((Number)value).longValue();
        }

        if (value instanceof String) {
            return Long.valueOf((String)value);
        }

        if (value instanceof Boolean) {
            return (Boolean)value ? 1 : 0;
        }

        return 0;
    }

    public static int asInt(Object value) {
        if (isNull(value))  return 0;

        if (value instanceof Number) {
            return ((Number)value).intValue();
        }

        if (value instanceof String) {
            return Integer.valueOf((String)value);
        }

        if (value instanceof Boolean) {
            return (Boolean)value ? 1 : 0;
        }

        return 0;
    }

    public static double asDouble(Object value) {
        if (isNull(value))  return 0.0;
        
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        
        if (value instanceof String) {
            return Double.parseDouble((String)value);
        }
        
        if (value instanceof Boolean) {
            return (Boolean)value ? 1.0 : 0.0;
        }
        
        return 0.0;
    }
    
    public static Date asDate(Object value) {
        if (isNull(value))  return null;
        
        if (value instanceof Date) {
            return (Date)value;
        }
        
        if (value instanceof String) {
            return parseDate((String)value);
        }
        
        return null; // TODO throw type cast exception
    }
    
    public static boolean isNull(Object value) {
        return value == null ||
                (value instanceof String && ((String) value).isEmpty());
    }
    
    /**
     * Returns the value as an object of the passed type. If the type conversion
     * cannot occur, <code>null</code> is returned.  Otherwise, the standard type
     * conversion occurs.
     * @return
     */
    public static Object asType(Object value, TwistDataType type) {
            switch (type) {
            case STRING:
                return asString(value);
            case BOOLEAN:
                return asBoolean(value);
            case DOUBLE:
                return asDouble(value);
            case INTEGER:
                return asInt(value);
            case DATETIME:
                return asDate(value);
            }
        return null;
    }
    
    public static String formatDate(Date date) {
        if (date == null) return null;

        return DateTimeFormatter.ISO_DATE_TIME.format(date.toInstant());
    }

    public static Date parseDate(String dateString) {
        if (dateString == null) return null;

        return Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(dateString)));
    }


    /**
     * works like a compare function in Comparable.
     * @param left
     * @param right
     * @return
     */
    public static int compare(Object left, Object right) {

        if (isNull(left) && isNull(right)) {
            return 0;
        }

        TwistDataType leftType = ValueUtils.getType(left);
        TwistDataType rightType = ValueUtils.getType(right);


        if (leftType == TwistDataType.STRING || rightType == TwistDataType.STRING) {
            return asSafeString(left).compareTo(asSafeString(right));
        }
        else if (leftType == TwistDataType.DOUBLE || rightType == TwistDataType.DOUBLE) {
            double l = asDouble(left);
            double r = asDouble(right);
            return Double.compare(l, r);
        }
        else if (leftType == TwistDataType.INTEGER || rightType == TwistDataType.INTEGER) {
            return asInt(left) - asInt(right);
        }
        else {
            return asSafeString(left).compareTo(asSafeString(right));
        }
    }
    
    //
    // Implementation
    //
    
    private static String asSafeString(Object value) {
        if (isNull(value)) {
            return "";
        }
        else {
            return asString(value);
        }
    }
}
