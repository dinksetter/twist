package com.inksetter.twist.expression;

import com.inksetter.twist.EvalContext;
import com.inksetter.twist.Expression;
import com.inksetter.twist.exec.ScriptContext;
import com.inksetter.twist.exec.StatementBlock;
import com.inksetter.twist.exec.UserDefFunction;

import java.util.List;

public class LambdaExpression implements Expression {
    private final List<String> identifiers;
    private final StatementBlock body;

    public LambdaExpression(List<String> identifiers, StatementBlock body) {
        this.identifiers = identifiers;
        this.body = body;
    }

    /**
     * Each evaluation produces a function that closes over the scope it was evaluated in, so a
     * lambda created inside a loop or a function call captures that particular scope.
     */
    public UserDefFunction evaluate(EvalContext ctx) {
        ScriptContext captured = (ctx instanceof ScriptContext) ? (ScriptContext) ctx : null;
        return new UserDefFunction("", identifiers, body, captured, true);
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "-> " + identifiers + " " + body;
    }
}
