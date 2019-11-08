package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetValueExpression<T> implements Expression<T> {

    private String name;

    public String getName() {
        return name;
    }

    private DataType type;

    public GetValueExpression(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public T getResult(Stack stack) {
        //System.out.println("try get value: " + name);
        return ((StackItem<T>) stack.getFromStack(name)).getResult(stack);
    }

    @Override
    public DataType getType() {
        return type;
    }
}
