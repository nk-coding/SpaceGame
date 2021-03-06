package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class ExternalMethodStatement<T> implements Statement, Expression<T> {

    //normally used to init
    protected Expression[] parameterExpressions = null;
    protected String name;
    private DataType type;

    public ExternalMethodStatement(DataType type) {
        this.type = type;
    }

    public void setParameterExpressions(Expression[] parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public T getResult(Stack stack) {
        Object[] parameters;
        if (parameterExpressions != null) {
            parameters = new Object[parameterExpressions.length];
            for (int x = 0; x < parameters.length; x++) {
                parameters[x] = parameterExpressions[x].getResult(stack);
            }
        } else {
            parameters = new Object[0];
        }
        ExternalMethodFuture future = new ExternalMethodFuture();
        future.setParameters(parameters);
        future.setName(name);
        future.setType(type);
        stack.getCallbackHandler().handleExternalMethod(future);
        try {
            return (T) future.get();
        } catch (Exception e) {
            System.out.println("Exception at extern method, " + name);
            return null;
        }
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
