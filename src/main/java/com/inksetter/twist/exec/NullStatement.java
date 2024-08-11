package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

public class NullStatement implements Statement {

    @Override
    public Object execute(ScriptContext exec) {
        return null;
    }
}
