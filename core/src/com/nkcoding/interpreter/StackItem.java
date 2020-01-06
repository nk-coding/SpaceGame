package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class StackItem<T> implements Expression<T> {
    private String name;
    private int stackLevel;
    private T value;
    private DataType type;

    public StackItem(DataType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStackLevel() {
        return stackLevel;
    }

    public void setStackLevel(int stackLevel) {
        this.stackLevel = stackLevel;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public T getResult(Stack stack) {
        return value;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " " + name + " = " + value.toString();
    }
}
