package com.nkcoding.interpreter;

import com.nkcoding.interpreter.compiler.DataType;

public class StackItem<T> implements Expression<T> {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int stackLevel;

    public int getStackLevel() {
        return stackLevel;
    }

    public void setStackLevel(int stackLevel) {
        this.stackLevel = stackLevel;
    }

    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    private DataType type;

    public StackItem(DataType type) {
        this.type = type;
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
