package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

import java.util.List;

public class MethodCallExpression implements Expression {

    public MethodCallExpression(Expression target, String memberName, List<Expression> methodArgs) {
        _target = target;
        _memberName = memberName;
        _methodArgs = methodArgs;
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
    private final List<Expression> _methodArgs;
}
