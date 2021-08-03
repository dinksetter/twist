package com.inksetter.twist.expression;

import com.inksetter.twist.exec.ExecContext;
import com.inksetter.twist.TwistException;

public class MemberExpression implements Expression {

    public MemberExpression(Expression target, String memberName) {
        _target = target;
        _memberName = memberName;
    }

    @Override
    public Object evaluate(ExecContext ctx) throws TwistException {
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
