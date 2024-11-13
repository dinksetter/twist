package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

public interface Statement {
    StatementResult execute(ScriptContext exec) throws TwistException;
}
