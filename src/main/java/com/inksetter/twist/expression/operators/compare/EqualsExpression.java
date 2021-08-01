package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

public class EqualsExpression extends AbsractOperExpression {
    public EqualsExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    /**
     * Compare two values for equality.  
     * 
     * @param left any data value.  This parameter may not be null.
     * @param right any data value.  This parameter may not be null
     * @return true, if the values are considered equal
     */
    public static boolean valueEquality(TwistValue left, TwistValue right) {
        if (left.isNull() && right.isNull()) return true;
        if (left.isNull() || right.isNull()) return false;
        
        if (left.getType() == right.getType()) {
            return left.getValue().equals(right.getValue());
        }
        else if (left.getType() == TwistDataType.STRING || right.getType() == TwistDataType.STRING) {
            return left.asString().equals(right.asString());
        }
        else if (left.getType() == TwistDataType.DOUBLE || right.getType() == TwistDataType.DOUBLE) {
            return Double.doubleToLongBits(left.asDouble()) == Double
                .doubleToLongBits(right.asDouble());
        }
        else if (left.getType() == TwistDataType.INTEGER || right.getType() == TwistDataType.INTEGER) {
            return left.asInt() == right.asInt();
        }
        else if (left.getType() == TwistDataType.BOOLEAN || right.getType() == TwistDataType.BOOLEAN) {
            return left.asBoolean() == right.asBoolean();
        }
        else {
            return left.asString().equals(right.asString());
        }
    }
    
    protected TwistValue doOper(TwistValue left, TwistValue right) {
        return new TwistValue(TwistDataType.BOOLEAN, compare(left, right));
    }
    
    protected boolean compare(TwistValue left, TwistValue right) {
        return EqualsExpression.valueEquality(left, right);
    }

    @Override
    protected String operString() {
        return "=";
    }
}
