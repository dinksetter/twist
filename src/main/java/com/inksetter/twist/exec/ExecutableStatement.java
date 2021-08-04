package com.inksetter.twist.exec;

import com.inksetter.twist.TwistException;
import com.inksetter.twist.ValueUtils;
import com.inksetter.twist.expression.Expression;
import org.apache.log4j.Logger;

import java.util.List;

public class ExecutableStatement implements Expression {
    
    public void setIfTest(Expression ifTest) {
        _ifTest = ifTest;
    }

    public void setIfBlock(ExecutableStatement ifBlock) {
        _ifBlock = ifBlock;
    }

    public void setElseBlock(ExecutableStatement elseBlock) {
        _elseBlock = elseBlock;
    }

    public void setAssignment(String assignmentIdentifier) {
        _assignmentIdentifier = assignmentIdentifier;
    }

    public String getAssignment() {
        return _assignmentIdentifier;
    }

    public void setExpression(Expression expression) {
        _expression = expression;
    }

    public Expression getExpression() { return _expression; }

    public void setCatchBlocks(List<CatchBlock> catchBlocks) {
        _catchBlocks = catchBlocks;
    }

    public void setFinallyBlock(StatementSequence finallyBlock) {
        _finallyBlock = finallyBlock;
    }

    @Override
    public Object evaluate(ExecContext exec) throws TwistException {
        return _executeStatement(exec);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        if (_ifTest != null) {
            tmp.append("if (");
            tmp.append(_ifTest);
            tmp.append(") ");
            tmp.append(_ifBlock);
            if (_elseBlock != null) {
                tmp.append(" ELSE ");
                tmp.append(_elseBlock);
            }
        }

        else {
            if ((_catchBlocks != null && _catchBlocks.size() != 0)
                    || _finallyBlock != null) {
                tmp.append("try {");
                tmp.append(_subSequence);
                tmp.append('}');
                if (_catchBlocks != null) {
                    for (CatchBlock cblock : _catchBlocks) {
                        tmp.append(cblock);
                    }
                }
                if (_finallyBlock != null) {
                    tmp.append("finally");
                    tmp.append(_finallyBlock);
                }
            }
            else {
                if (_subSequence != null) {
                    tmp.append(_subSequence);
                }
                else {
                    if (_assignmentIdentifier != null) {
                        tmp.append(_assignmentIdentifier).append(" = ");
                    }
                    tmp.append(_expression).append(";");
                }
            }
        }

        return tmp.toString();
    }


    private Object _executeStatement(ExecContext exec) throws TwistException {
        if (_ifTest != null) {
            _logger.debug("if (" + _ifTest + ") ... ");
            Object testValue = _ifTest.evaluate(exec);
            if (ValueUtils.asBoolean(testValue)) {
                _logger.debug("If-test passed - executing if block");
                _ifBlock.evaluate(exec);
            }
            else if (_elseBlock != null) {
                _logger.debug("If-test failed - executing else block");
                _elseBlock.evaluate(exec);
            }
            else {
                _logger.debug("If-test failed - no else block to execute");
            }

            // If statements do not have value, just side effects.
            return null;
        }

        if (_subSequence != null) {
            try {
                return _subSequence.execute(exec, true);
            } catch (Exception e) {
                if (_catchBlocks != null) {
                    // If we're set up to catch errors, do so.
                    String exceptionClassName = e.getClass().getTypeName();
                    for (CatchBlock catchBlock : _catchBlocks) {

                        boolean matches = exceptionClassName.equals(catchBlock.getTypeName());

                        if (matches) {
                            _logger.debug("Catch condition met - executing catch block...");
                            // We execute the catch block, if it exists. If it's a
                            // simple catch expression, then
                            // we return the error results of the exception that got
                            // thrown.
                            StatementSequence block = catchBlock.getBlock();

                            // If there's a block of code to execute on this catch
                            // expression, return the result of executing that
                            // block.
                            if (block != null) {
                                block.execute(exec, true);
                                break;
                            }
                        }
                    }
                    _logger.debug("No catch expression matched throwing exception");
                }

                // If we got through the entire catch expression set, throw the
                // original exception out.
                throw e;
            } finally {
                if (_finallyBlock != null) {
                    _logger.debug("Executing finally block...");
                    _finallyBlock.execute(exec, true);
                }
            }
        }
        else {
            if (_expression != null) {
                Object value = _expression.evaluate(exec);
                if (_assignmentIdentifier != null) {
                    exec.setVariable(_assignmentIdentifier, value);
                }
                return value;
            }
        }
        return null;
    }

    private static final Logger _logger =
        Logger.getLogger(ExecutableStatement.class);

    private Expression _ifTest;

    private ExecutableStatement _ifBlock;
    private ExecutableStatement _elseBlock;
    private String _assignmentIdentifier;
    private Expression _expression;

    private List<CatchBlock> _catchBlocks;
    private StatementSequence _finallyBlock;
    private StatementSequence _subSequence;

    public void setSubSequence(StatementSequence subSequence) {
        _subSequence = subSequence;
    }
}
