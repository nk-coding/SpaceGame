package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;

public class MethodWrapperStatement<T> implements Statement, Expression<T> {

    protected MethodStatement methodStatement;

    public void setMethodStatement(MethodStatement methodStatement) {
        this.methodStatement = methodStatement;
    }

    //normally used to init
    protected Statement[] initStatements = null;

    public void setInitStatements(Statement[] initStatements) {
        this.initStatements = initStatements;
    }

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    private String type;

    public MethodWrapperStatement(String type) {
        this.type = type;
    }

    @Override
    public T getResult(Stack stack) {
        stack.beginStackLevel();
        //init the parameters
        if (initStatements != null) {

            for (Statement statement : initStatements) {
                try {
                    statement.run(stack);
                } catch (ReturnException e) {
                    System.out.println("something went wrong: init statement throws exception");
                    e.printStackTrace();
                }
            }
        }
        //init result value
        //don't do it if it has no return type
        if (!type.equals(DataTypes.Void)) stack.<T>addToStack(name + "$result", null, type);
        //run the method
        try {
            methodStatement.run(stack);
        } catch (ReturnException e) {
            //that's ok
        }
        //save the result
        T result = (type.equals(DataTypes.Void)) ? null : ((StackItem<T>) stack.getFromStack(name + "$result")).getResult(stack);
        stack.clearStackLevel();
        return result;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }


    @Override
    public String getType() {
        return type;
    }
}
