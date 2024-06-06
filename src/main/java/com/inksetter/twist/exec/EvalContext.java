

package com.inksetter.twist.exec;

import java.util.List;

public interface EvalContext {

    Object lookup(String name);

    boolean functionExists(String functionName);

    Object callFunction(String functionName, List<Object> argValues);
}