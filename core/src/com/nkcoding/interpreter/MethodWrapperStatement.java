package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class MethodWrapperStatement<T> implements Statement, Expression<T> {

    protected MethodStatement methodStatement;
    //normally used to init
    protected Statement[] initStatements = null;
    protected String name;
    private DataType type;

    public MethodWrapperStatement(DataType type) {
        this.type = type;
    }

    public void setMethodStatement(MethodStatement methodStatement) {
        this.methodStatement = methodStatement;
    }

    public void setInitStatements(Statement[] initStatements) {
        this.initStatements = initStatements;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!type.equals(DataType.VOID)) stack.<T>addToStack(name + "$result", null, type);
        //run the method
        try {
            methodStatement.run(stack);
        } catch (ReturnException e) {
            //that's ok
        }
        //save the result
        Object result;
        if (type.equals(DataType.VOID)) {
            result = null;
        } else {
            StackItem stackItem = stack.getFromStack(name + "$result");
            result = stackItem.getResult(stack);
            if (result == null) {
                //shame on this wrong implemented method
                result = type.getDefaultValue();
            }
        }
        stack.clearStackLevel();
        return (T) result;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }


    @Override
    public DataType getType() {
        return type;
    }
}
