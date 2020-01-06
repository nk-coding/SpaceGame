package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

import java.util.function.Function;

public class PredefinedMethodStatement<T> implements Statement, Expression<T> {

    //normally used to init
    protected Expression[] parameterExpressions = null;
    protected String name;
    protected Function<Object[], T> predefinedMethod;
    private DataType type;

    public PredefinedMethodStatement(DataType type) {
        this.type = type;
    }

    public void setParameterExpressions(Expression[] parameterExpressions) {
        this.parameterExpressions = parameterExpressions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPredefinedMethod(Function<Object[], T> predefinedMethod) {
        this.predefinedMethod = predefinedMethod;
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
        return predefinedMethod.apply(parameters);
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
