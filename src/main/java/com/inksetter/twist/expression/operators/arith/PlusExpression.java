package com.inksetter.twist.expression.operators.arith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class PlusExpression extends AbsractOperExpression {
    public PlusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        if (left.getType() == TwistDataType.DATETIME) {
            return addToDate(left, right);
        }
        else if (right.getType() == TwistDataType.DATETIME) {
            return (addToDate(right, left));
        }
        else if (left.getType() == TwistDataType.STRING) {
            return new TwistValue(left.asString() + right.asString());
        }
        else {
            if (left.getType() == TwistDataType.DOUBLE || right.getType() == TwistDataType.DOUBLE) {
                return new TwistValue(TwistDataType.DOUBLE, left.asDouble() + right.asDouble());
            }
            else {
                return new TwistValue(TwistDataType.INTEGER, left.asInt() + right.asInt());
            }
        }
    }

    @Override
    protected String operString() {
        return " + ";
    }
    
    private TwistValue addToDate(TwistValue date, TwistValue days) {
        Date d = date.asDate();

        // If the left side is null, return a null result.
        if (d == null) {
            return new TwistValue(TwistDataType.DATETIME, null);
        }
        
        TwistDataType daysType = days.getType();
        if (daysType == TwistDataType.INTEGER || daysType == TwistDataType.DOUBLE) {
            LocalDateTime dt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());

            long wholeDays = days.asInt();
            double dayPart = days.asDouble() - wholeDays;
            long msDiff = (long)(dayPart * 1000.0 * 3600.0 * 24.0);
            
            dt = dt.plusDays(wholeDays).plus(msDiff, ChronoUnit.MILLIS);
            
            return new TwistValue(TwistDataType.DATETIME, Date.from(dt.atZone(ZoneId.systemDefault()).toInstant()));
        }
        else {
            return date;
        }
    }

}
