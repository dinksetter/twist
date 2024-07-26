package com.inksetter.twist;

import com.inksetter.twist.exec.ScriptContext;

public interface Script {
    Object execute(ScriptContext exec) throws TwistException;
}
