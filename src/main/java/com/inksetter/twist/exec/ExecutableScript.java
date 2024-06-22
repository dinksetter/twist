package com.inksetter.twist.exec;

import com.inksetter.twist.ScriptContext;
import com.inksetter.twist.TwistException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExecutableScript implements Serializable {
    private final List<ExecutableStatement> statements = new ArrayList<>();
    private static final long serialVersionUID = 6843629792467515246L;

    public List<ExecutableStatement> getStatements() {
        return statements;
    }
    
    public void addStatement(ExecutableStatement statement) {
        statements.add(statement);
    }
    
    public Object execute(ScriptContext exec, boolean newStack) throws TwistException {
        if (newStack) exec.pushStack();
        Object lastValue = null;
        try {
            for (ExecutableStatement statement : statements) {
                lastValue = statement.execute(exec);
            }
        }
        finally {
            if (newStack) exec.popStack();
        }
        return lastValue;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder("{");
        for (Iterator<ExecutableStatement> i = statements.iterator(); i.hasNext();) {
            ExecutableStatement stream = i.next();
            tmp.append(stream);
            if (i.hasNext()) tmp.append("; ");
        }
        tmp.append('}');
        return tmp.toString();
    }

}
