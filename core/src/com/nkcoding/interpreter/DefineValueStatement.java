package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class DefineValueStatement<T> implements Statement {

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    protected Expression<T> valueExpression = null;

    public void setValueExpression(Expression<T> valueExpression) {
        this.valueExpression = valueExpression;
    }

    private DataType type;

    public DefineValueStatement(DataType type) {
        this.type = type;
    }


    @Override
    public void run(Stack stack) throws ReturnException {
        stack.addToStack(name, valueExpression != null ? valueExpression.getResult(stack) : (T) getAlternativeValue(), type);
    }

    private Object getAlternativeValue() {
        switch (type.name) {
            case DataType.FLOAT_KW:
                return (float) 0;
            case DataType.INTEGER_KW:
                return 0;
            case DataType.BOOLEAN_KW:
                return false;
            case DataType.STRING_KW:
                return "";
            default:
                throw new IllegalStateException("cannot create alternative value");
        }
    }
}
