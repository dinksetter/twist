package com.inksetter.twist.expression.operators.compare;

import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.Expression;
import com.inksetter.twist.expression.operators.AbsractOperExpression;

import java.util.Objects;

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
    public static boolean valueEquality(Object left, Object right) {
        
        TwistDataType leftType = ValueUtils.getType(left);
        TwistDataType rightType = ValueUtils.getType(right);

        if (leftType == TwistDataType.INTEGER || rightType == TwistDataType.INTEGER) {
            return ValueUtils.asInt(left) == ValueUtils.asInt(right);
        }
        else if (leftType == TwistDataType.DOUBLE || rightType == TwistDataType.DOUBLE) {
            return ValueUtils.asDouble(left) == ValueUtils.asDouble(right);
        }
        
        return Objects.equals(left, right);
    }
    
    @Override
    protected Object doOper(Object left, Object right) {
        return compare(left, right);
    }
    
    protected boolean compare(Object left, Object right) {
        return EqualsExpression.valueEquality(left, right);
    }

    @Override
    protected String operString() {
        return "=";
    }
}
