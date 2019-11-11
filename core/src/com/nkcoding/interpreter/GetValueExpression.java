package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetValueExpression<T> implements Expression<T> {

    private GetStackItem getStackItem;

    public GetStackItem getGetStackItem() {
        return getStackItem;
    }

    public GetValueExpression(GetStackItem getStackItem) {
        this.getStackItem = getStackItem;
    }

    @Override
    public T getResult(Stack stack) {
        //System.out.println("try get value: " + name);
        return (T) getStackItem.getStackItem(stack).getResult(stack);
    }

    @Override
    public DataType getType() {
        return getStackItem.getType();
    }
}
