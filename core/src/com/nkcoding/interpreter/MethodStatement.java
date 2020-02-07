package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.MethodDefinition;

public class MethodStatement implements Statement {

    private final MethodDefinition definition;
    protected Statement[] statements;

    //constructor
    public MethodStatement(MethodDefinition definition) {
        this.definition = definition;
    }

    public Statement[] getStatements() {
        return statements;
    }

    public void setStatements(Statement[] statements) {
        this.statements = statements;
    }

    public MethodDefinition getDefinition() {
        return definition;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        //the stack stuff is done in a wrapper
        for (Statement statement : statements) {
            statement.run(stack);
        }
    }
}
