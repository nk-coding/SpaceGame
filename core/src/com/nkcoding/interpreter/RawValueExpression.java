package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class RawValueExpression<T> implements Expression<T> {

    private final T value;

    private DataType type;

    public RawValueExpression(T value, DataType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public T getResult(Stack stack) {
        //System.out.println("Raw value expression: return " + value);
        return value;
    }

    @Override
    public DataType getType() {
        return type;
    }
}
