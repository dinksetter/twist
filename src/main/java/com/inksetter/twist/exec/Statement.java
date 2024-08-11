package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.Expression;

import java.io.Serializable;
import java.util.List;

public interface Statement {
    Object execute(ScriptContext exec) throws TwistException;
}
