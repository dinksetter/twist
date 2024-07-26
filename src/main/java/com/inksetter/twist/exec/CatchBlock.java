package com.inksetter.twist.exec;

public class CatchBlock {

    private String typeName;
    private String varName;
    private StatementBlock block;

    public void setType(String typeName) {
        this.typeName = typeName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public void setBlock(StatementBlock block) {
        this.block = block;
    }
    
    public String getTypeName() {
        return typeName;
    }

    public String getVarName() {
        return varName;
    }

    /**
     * @return Returns the block.
     */
    public StatementBlock getBlock() {
        return block;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append("catch(");
        tmp.append(typeName);
        tmp.append(" ").append(varName).append(")");
        if (block != null) {
            tmp.append(block);
        }
        else {
            tmp.append("{ /* empty */ }");
        }
        return tmp.toString();
    }

}
