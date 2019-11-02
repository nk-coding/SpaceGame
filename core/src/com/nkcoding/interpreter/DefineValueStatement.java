package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataTypes;

public class DefineValueStatement<T> implements Statement {

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    protected Expression<T> valueExpression = null;

    public void setValueExpression(Expression<T> valueExpression) {
        this.valueExpression = valueExpression;
    }

    private String type;

    public DefineValueStatement(String type) {
        this.type = type;
    }


    @Override
    public void run(Stack stack) throws ReturnException {
        stack.addToStack(name, valueExpression != null ? valueExpression.getResult(stack) : (T) getAlternativeValue(), type);
    }

    private Object getAlternativeValue() {
        switch (type) {
            case DataTypes.Float:
                return (float) 0;
            case DataTypes.Integer:
                return 0;
            case DataTypes.Boolean:
                return false;
            case DataTypes.String:
                return "";
            default:
                throw new IllegalStateException("missing a data type");
        }
    }
}
