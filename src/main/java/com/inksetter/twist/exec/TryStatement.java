package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;

import java.util.ArrayList;
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
    public StatementResult execute(ScriptContext exec) throws TwistException {
        try {
            return mainBlock.execute(exec, true);
        } catch (Exception e) {
            if (catchBlocks != null) {
                // If we're set up to catch errors, do so.
                // Exceptions thrown by Java code are wrapped in a TwistException. A catch block can
                // name either the wrapper or the exception that caused it.
                List<Throwable> candidates = new ArrayList<>();
                for (Throwable t = e; t != null; t = t.getCause()) {
                    candidates.add(t);
                    if (!(t instanceof TwistException)) {
                        break;
                    }
                }

                for (CatchBlock catchBlock : catchBlocks) {
                    Throwable matched = match(catchBlock, candidates);
                    if (matched != null) {
                        // We execute the catch block, if it exists. If it's a
                        // simple catch expression, then
                        // we return the error results of the exception that got
                        // thrown.
                        StatementBlock block = catchBlock.getBlock();

                        // If there's a block of code to execute on this catch
                        // expression, return the result of executing that
                        // block.
                        if (block != null) {
                            String varName = catchBlock.getVarName();
                            exec.pushStack(false);
                            exec.setVariable(varName, matched);
                            try {
                                return block.execute(exec, true);
                            }
                            finally {
                                exec.popStack();
                            }
                        }

                        // An empty catch block swallows the exception.
                        return StatementResult.valueResult(null);
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

    /**
     * Returns the first of the candidate exceptions whose type, or one of its supertypes, is named
     * by the given catch block, or null if the catch block doesn't apply.
     */
    private Throwable match(CatchBlock catchBlock, List<Throwable> candidates) {
        // Deepest cause first, so that a general catch block like catch (Exception e) binds the
        // exception that was originally thrown rather than the TwistException wrapping it.
        for (int i = candidates.size() - 1; i >= 0; i--) {
            Throwable caught = candidates.get(i);
            for (Class<?> cls = caught.getClass(); cls != null; cls = cls.getSuperclass()) {
                if (catchBlock.getTypeName().equals(cls.getSimpleName())) {
                    return caught;
                }
            }
        }
        return null;
    }
}
