package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

public class BlockStatement implements Statement {
    private final StatementBlock mainBlock;

    public BlockStatement(StatementBlock mainBlock) {
        this.mainBlock = mainBlock;
    }

    @Override
    public Object execute(ScriptContext exec) throws TwistException {
        return mainBlock.execute(exec, true);
    }
}
