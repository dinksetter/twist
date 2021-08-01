package com.inksetter.twist.expression.operators.arith;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
                
                LocalDateTime dt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                                
                int wholeDays = right.asInt();
                double dayPart = right.asDouble() - wholeDays;
                int msDiff = (int)(dayPart * 1000.0 * 3600.0 * 24.0);
                
                dt = dt.minusDays(wholeDays).minus(msDiff, ChronoUnit.MILLIS);
                
                return new TwistValue(TwistDataType.DATETIME, Date.from(dt.atZone(ZoneId.systemDefault()).toInstant()));
            }
            else if (right.getType() == TwistDataType.DATETIME) {
                Date leftDate = left.asDate();
                Date rightDate = right.asDate();
                
                // If either the left side or the right side is null, return null
                if (leftDate == null || rightDate == null) {
                    return new TwistValue(TwistDataType.DOUBLE, null);
                }
                LocalDateTime leftDt = LocalDateTime.ofInstant(leftDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime rightDt = LocalDateTime.ofInstant(rightDate.toInstant(), ZoneId.systemDefault());

                Duration diff = Duration.between(leftDt, rightDt);

                long fullDays = diff.toDays();
                long ms = diff.toMillis() - (fullDays * 1000 * 3600 * 24);

                double partial = ((double)ms / (1000.0 * 3600.0 * 24.0));

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
