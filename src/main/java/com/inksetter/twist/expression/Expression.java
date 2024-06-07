package com.inksetter.twist.expression;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.exec.SymbolSource;

public interface Expression {
    Object evaluate(SymbolSource ctx) throws TwistException;
}
