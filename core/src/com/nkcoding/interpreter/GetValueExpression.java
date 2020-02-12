package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetValueExpression<T> implements Expression<T> {

    private GetStackItem getStackItem;

    public GetValueExpression(GetStackItem getStackItem) {
        this.getStackItem = getStackItem;
    }

    public GetStackItem getGetStackItem() {
        return getStackItem;
    }

    @Override
    public T getResult(Stack stack) {
        return (T) getStackItem.getStackItem(stack).getResult(stack);
    }

    @Override
    public DataType getType() {
        return getStackItem.getType();
    }
}
