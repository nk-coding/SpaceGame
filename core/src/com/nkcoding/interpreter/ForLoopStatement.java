package com.nkcoding.interpreter;

public class ForLoopStatement extends StackStatement {

    protected Statement initStatement = null;
    protected Expression<Boolean> runCondition;
    protected Statement stepStatement;
    protected Statement[] statements;

    public void setInitStatement(Statement initStatement) {
        this.initStatement = initStatement;
    }

    public void setRunCondition(Expression<Boolean> runCondition) {
        this.runCondition = runCondition;
    }

    public void setStepStatement(Statement stepStatement) {
        this.stepStatement = stepStatement;
    }

    public void setStatements(Statement[] statements) {
        this.statements = statements;
    }

    @Override
    void runOverride(Stack stack) throws ReturnException {
        //System.out.println("run override in for loop statement");
        //run init statement once if it exists
        if (initStatement != null) {
            //System.out.println("for loop: run init statement");
            try {
                initStatement.run(stack);
            } catch (ReturnException e) {
                System.out.println("return exception is not allowed in init statement of for loop");
            }
            //System.out.println("for loop end run init statement");
        }
        //System.out.println("run condition: " + runCondition.getResult(stack));
        while (runCondition.getResult(stack)) {
            //System.out.println("in while loop of for loop statement");
            for (Statement statement : statements) {
                statement.run(stack);
            }
            if (stepStatement != null) {
                try {
                    stepStatement.run(stack);
                } catch (ReturnException e) {
                    System.out.println("return exception is not allowed in step statement of for loop");
                }
            }
        }
    }
}
