package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetterExpression<T> implements Expression<T> {

    private Expression<ListObject> listExpression;

    private int index;

    public GetterExpression(Expression<ListObject> listExpression, int index) {
        this.listExpression = listExpression;
        this.index = index;
    }

    @Override
    public T getResult(Stack stack) {
        return (T)listExpression.getResult(stack).items[index].getResult(stack);
    }

    @Override
    public DataType getType() {
        return listExpression.getType().listTypes[index].getType();
    }
}
