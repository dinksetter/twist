package com.inksetter.twist.exec;

import com.inksetter.twist.Expression;
import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;

import java.util.List;

public class TryStatement implements Statement {
    private final StatementBlock mainBlock;
    private final List<CatchBlock> catchBlocks;
    private final StatementBlock finallyBlock;

    public TryStatement(StatementBlock mainBlock, List<CatchBlock> catchBlocks, StatementBlock finallyBlock) {
        this.mainBlock = mainBlock;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public Object execute(ScriptContext exec) throws TwistException {
        try {
            return mainBlock.execute(exec, true);
        } catch (Exception e) {
            if (catchBlocks != null) {
                // If we're set up to catch errors, do so.
                String exceptionClassName = e.getClass().getTypeName();
                for (CatchBlock catchBlock : catchBlocks) {

                    boolean matches = exceptionClassName.equals(catchBlock.getTypeName());

                    if (matches) {
                        // We execute the catch block, if it exists. If it's a
                        // simple catch expression, then
                        // we return the error results of the exception that got
                        // thrown.
                        StatementBlock block = catchBlock.getBlock();

                        // If there's a block of code to execute on this catch
                        // expression, return the result of executing that
                        // block.
                        if (block != null) {
                            block.execute(exec, true);
                            break;
                        }
                    }
                }
            }

            // If we got through the entire catch expression set, throw the
            // original exception out.
            throw e;
        } finally {
            if (finallyBlock != null) {
                finallyBlock.execute(exec, true);
            }
        }
    }
}
