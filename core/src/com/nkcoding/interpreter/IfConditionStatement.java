package com.nkcoding.interpreter;

public class IfConditionStatement implements Statement {

    protected Expression<Boolean> condition;
    protected Statement[] statements;
    protected Statement elseStatement = null;

    public void setCondition(Expression<Boolean> condition) {
        this.condition = condition;
    }

    public void setStatements(Statement[] statements) {
        this.statements = statements;
    }

    public void setElseStatement(Statement elseStatement) {
        this.elseStatement = elseStatement;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        if (condition.getResult(stack)) {
            stack.beginStackLevel();
            try {
                for (Statement statement : statements) {
                    statement.run(stack);
                }
            } finally {
                stack.clearStackLevel();
            }
        } else if (elseStatement != null) {
            elseStatement.run(stack);
        }
    }
}
