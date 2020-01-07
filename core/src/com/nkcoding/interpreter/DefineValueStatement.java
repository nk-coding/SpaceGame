package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class DefineValueStatement<T> implements Statement {

    protected String name;
    protected Expression<T> valueExpression = null;
    private DataType type;

    public DefineValueStatement(DataType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValueExpression(Expression<T> valueExpression) {
        this.valueExpression = valueExpression;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        stack.addToStack(name, valueExpression != null ? valueExpression.getResult(stack) : (T) type.getDefaultValue(), type);
    }
}
