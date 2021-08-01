package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistDataType;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

public class ReferenceExpression implements Expression {
    public ReferenceExpression(String name) {
        _name = name;
    }
    
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        TwistValue tmp = ctx.getVariable(_name);

        if (tmp == null) {
            return new TwistValue(TwistDataType.STRING, null);
        } else {
            return tmp;
        }
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _name;
    }

    private final String _name;

}
