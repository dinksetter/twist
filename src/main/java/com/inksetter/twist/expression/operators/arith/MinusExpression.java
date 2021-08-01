package com.inksetter.twist.expression.operators.arith;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class MinusExpression extends AbsractOperExpression {
    public MinusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        if (left.getType() == TwistDataType.DATETIME) {
            if (right.getType() == TwistDataType.DOUBLE || right.getType() == TwistDataType.INTEGER) {
                Date d = left.asDate();

                // If the left side is null, return a null result.
                if (d == null) {
                    return new TwistValue(TwistDataType.DATETIME, null);
                }
                
                LocalDateTime dt = new LocalDateTime(d);
                                
                int wholeDays = right.asInt();
                double dayPart = right.asDouble() - wholeDays;
                int msDiff = (int)(dayPart * 1000.0 * 3600.0 * 24.0);
                
                dt = dt.minusDays(wholeDays).minusMillis(msDiff);                
                
                return new TwistValue(TwistDataType.DATETIME, dt.toDateTime().toDate());
            }
            else if (right.getType() == TwistDataType.DATETIME) {
                Date leftDate = left.asDate();
                Date rightDate = right.asDate();
                
                // If either the left side or the right side is null, return null
                if (leftDate == null || rightDate == null) {
                    return new TwistValue(TwistDataType.DOUBLE, null);
                }
                
                DateTime leftDt = new DateTime(leftDate);
                DateTime rightDt = new DateTime(rightDate);
                
                int fullDays = Days.daysBetween(rightDt, leftDt).getDays();
                
                LocalTime leftTime = new LocalTime(leftDt);
                LocalTime rightTime = new LocalTime(rightDt);
                
                int ms = leftTime.getMillisOfDay() - rightTime.getMillisOfDay();
                double partial = ((double)ms / (1000.0 * 3600.0 * 24.0));
                
                if (partial < 0.0 && leftDate.after(rightDate)) {
                    partial += 1.0;
                }
                else if (partial > 0.0 && rightDate.after(leftDate)) {
                    partial -= 1.0;
                }
                
                double daysDiff = (double) fullDays + partial;
                
                return new TwistValue(TwistDataType.DOUBLE, daysDiff);
            }
        }
        else {
            if (left.getType() == TwistDataType.DOUBLE || right.getType() == TwistDataType.DOUBLE) {
                return new TwistValue(TwistDataType.DOUBLE, left.asDouble() - right.asDouble());
            }
            else {
                return new TwistValue(TwistDataType.INTEGER, left.asInt() - right.asInt());
            }
        }
        return null;
    }

    @Override
    protected String operString() {
        return " - ";
    }

}
