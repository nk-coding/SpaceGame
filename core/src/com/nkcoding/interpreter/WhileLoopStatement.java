package com.nkcoding.interpreter;

public class WhileLoopStatement extends StackStatement {

    protected Expression<Boolean> runCondition;
    protected Statement[] statements;

    public void setRunCondition(Expression<Boolean> runCondition) {
        this.runCondition = runCondition;
    }

    public void setStatements(Statement[] statements) {
        this.statements = statements;
    }

    @Override
    void runOverride(Stack stack) {
        while (runCondition.getResult(stack)) {
            for (Statement statement : statements) {
                try {
                    statement.run(stack);
                } catch (ReturnException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
