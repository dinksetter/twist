package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.ExecContext;

public interface Expression {
    Object evaluate(ExecContext ctx) throws TwistException;
}
