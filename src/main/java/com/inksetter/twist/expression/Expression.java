package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.TwistValue;
import com.inksetter.twist.exec.ExecContext;

public interface Expression {
    TwistValue evaluate(ExecContext ctx) throws TwistException;
}
