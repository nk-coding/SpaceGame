package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.MethodDefinition;

public class MethodStatement implements Statement {

    protected Statement[] statements;

    public void setStatements(Statement[] statements) {
        this.statements = statements;
    }

    public Statement[] getStatements() {
        return statements;
    }

    private final MethodDefinition definition;

    public MethodDefinition getDefinition() {
        return definition;
    }

    //constructor
    public MethodStatement(MethodDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        //the stack stuff is done in a wrapper
        //System.out.println("run method statement");
        for (Statement statement : statements) {
            statement.run(stack);
        }
    }
}
