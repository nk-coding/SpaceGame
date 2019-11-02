package com.nkcoding.interpreter;

public class StatementBlock extends StackStatement {

    protected Statement[] statements;

    public StatementBlock(Statement[] statements) {
        this.statements = statements;
    }

    @Override
    void runOverride(Stack stack) throws ReturnException {
        for (Statement statement : statements) {
            statement.run(stack);
        }
    }
}
