package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class ConcurrentStackItem<T> extends StackItem<T> {

    //default constructor
    public ConcurrentStackItem(DataType type, T value) {
        super(type);
        setValue(value);
    }

    @Override
    public synchronized void setValue(T value) {
        super.setValue(value);
    }

    @Override
    public synchronized T getResult(Stack stack) {
        return super.getResult(stack);
    }
}
