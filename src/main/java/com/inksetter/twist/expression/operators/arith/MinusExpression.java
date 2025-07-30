package com.inksetter.twist.expression.operators.arith;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.Expression;
import com.inksetter.twist.expression.TypeMismatchException;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class MinusExpression extends AbsractOperExpression {
    public MinusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected Object doOper(Object left, Object right) throws TwistException {
        if (ValueUtils.getType(left) == TwistDataType.DATETIME) {
            if (right == null) {
                return left;
            }
            else if (ValueUtils.getType(right) == TwistDataType.DATETIME) {
                Date leftDate = ValueUtils.asDate(left);
                Date rightDate = ValueUtils.asDate(right);

                // If either the left side or the right side is null, return null
                if (leftDate == null || rightDate == null) {
                    return null;
                }
                LocalDateTime leftDt = LocalDateTime.ofInstant(leftDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime rightDt = LocalDateTime.ofInstant(rightDate.toInstant(), ZoneId.systemDefault());

                Duration diff = Duration.between(rightDt, leftDt);

                long fullDays = diff.toDays();
                long ms = diff.toMillis() - (fullDays * 1000 * 3600 * 24);

                double partial = ((double) ms / (1000.0 * 3600.0 * 24.0));

                return (double) fullDays + partial;
            } else if (right instanceof Number) {
                Date d = ValueUtils.asDate(left);

                // If the left side is null, return a null result.
                if (d == null) {
                    return null;
                }

                LocalDateTime dt = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());

                long wholeDays = ((Number) right).longValue();
                double dayPart = ((Number) right).doubleValue() - wholeDays;
                long msDiff = (long) (dayPart * 1000.0 * 3600.0 * 24.0);

                dt = dt.minusDays(wholeDays).minus(msDiff, ChronoUnit.MILLIS);

                return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
            }
            else {
                throw new TypeMismatchException("Expected number, got " + right);
            }
        } else if (left instanceof Double || right instanceof Double) {
            return ValueUtils.asDouble(left) - ValueUtils.asDouble(right);
        } else {
            return ValueUtils.asInt(left) - ValueUtils.asInt(right);
        }
    }

    @Override
    protected String operString() {
        return " - ";
    }

}
