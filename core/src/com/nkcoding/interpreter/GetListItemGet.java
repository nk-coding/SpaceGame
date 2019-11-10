package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetListItemGet implements GetStackItem {
    private Expression<ListObject> listObject;

    private int index;

    public GetListItemGet(Expression<ListObject> listObject, int index) {
        this.listObject = listObject;
        this.index = index;
    }

    @Override
    public StackItem getStackItem(Stack stack) {
        return listObject.getResult(stack).items[index];
    }

    @Override
    public DataType getType() {
        return listObject.getType().listTypes[index].getType();
    }
}
