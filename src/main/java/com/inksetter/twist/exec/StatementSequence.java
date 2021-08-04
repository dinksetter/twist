package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StatementSequence implements Serializable {

    public List<ExecutableStatement> getStatements() {
        return _statements;
    }
    
    public void addStatement(ExecutableStatement statement) {
        _statements.add(statement);
    }
    
    public Object execute(ExecContext exec, boolean newStack) throws TwistException {
        if (newStack) exec.pushStack();
        Object lastValue = null;
        try {
            for (ExecutableStatement statement : _statements) {
                lastValue = statement.evaluate(exec);
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
        for (Iterator<ExecutableStatement> i = _statements.iterator(); i.hasNext();) {
            ExecutableStatement stream = i.next();
            tmp.append(stream);
            if (i.hasNext()) tmp.append("; ");
        }
        tmp.append('}');
        return tmp.toString();
    }
    

    // Private members
    private final List<ExecutableStatement> _statements = new ArrayList<>();
    private static final long serialVersionUID = 6843629792467515246L;
}
