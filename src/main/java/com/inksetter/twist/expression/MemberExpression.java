package com.inksetter.twist.expression;

import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;

public class MemberExpression implements Expression {

    public MemberExpression(String memberName, Expression target) {
        _memberName = memberName;
        _target = target;
    }

    @Override
    public TwistValue evaluate(ExecContext ctx) throws TwistException {
        return null;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _target + "." + _memberName;
    }

    private final Expression _target;
    private final String _memberName;
}
