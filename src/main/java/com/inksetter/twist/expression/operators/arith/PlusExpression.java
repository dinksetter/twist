package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class PlusExpression extends AbsractOperExpression {
    public PlusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Object doOper(Object left, Object right) {
        if (ValueUtils.getType(left) == TwistDataType.DATETIME) {
            return addToDate(ValueUtils.asDate(left), right);
        }
        else if (ValueUtils.getType(right) == TwistDataType.DATETIME) {
            return (addToDate(ValueUtils.asDate(right), left));
        }
        else if (left instanceof String) {
            return left + ValueUtils.asString(right);
        }
        else if (left instanceof Double || right instanceof Double) {
            return ValueUtils.asDouble(left) + ValueUtils.asDouble(right);
        }
        else {
            return ValueUtils.asInt(left) + ValueUtils.asInt(right);
        }
    }

    @Override
    protected String operString() {
        return " + ";
    }
    
    private Date addToDate(Date d, Object days) {

        // If the left side is null, return a null result.
        if (d == null) {
            return null;
        }
        else if (days instanceof Long || days instanceof Integer) {
            LocalDateTime dt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            long wholeDays = ((Number) days).longValue();
            dt = dt.plusDays(wholeDays);
            return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        }
        else if (days instanceof Number) {
            LocalDateTime dt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            long wholeDays = ((Number) days).longValue();
            double dayPart = (Double)days - wholeDays;
            long msDiff = (long)(dayPart * 1000.0 * 3600.0 * 24.0);
            dt = dt.plusDays(wholeDays).plus(msDiff, ChronoUnit.MILLIS);
            return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        }
        return d;
    }
}
