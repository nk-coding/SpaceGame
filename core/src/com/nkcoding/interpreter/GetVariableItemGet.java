package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class GetVariableItemGet implements GetStackItem {
    private String name;

    private DataType type;

    public GetVariableItemGet(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public StackItem getStackItem(Stack stack) {
        return stack.getFromStack(name);
    }

    @Override
    public DataType getType() {
        return type;
    }
}
