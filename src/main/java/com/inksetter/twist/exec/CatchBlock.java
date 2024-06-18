package com.inksetter.twist.exec;

public class CatchBlock {
    public void setType(String typeName) {
        _typeName = typeName;
    }

    public void setVarName(String varName) {
        _varName = varName;
    }

    public void setBlock(ExecutableScript block) {
        _block = block;
    }
    
    public String getTypeName() {
        return _typeName;
    }

    public String getVarName() {
        return _varName;
    }

    /**
     * @return Returns the block.
     */
    public ExecutableScript getBlock() {
        return _block;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append("catch(");
        tmp.append(_typeName);
        tmp.append(" ").append(_varName).append(")");
        if (_block != null) {
            tmp.append(_block);
        }
        else {
            tmp.append("{ /* empty */ }");
        }
        return tmp.toString();
    }
    
    private String _typeName;
    private String _varName;
    private ExecutableScript _block;
}
