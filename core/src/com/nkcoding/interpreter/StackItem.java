package com.nkcoding.interpreter;

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

    private String type;

    public StackItem(String type) {
        this.type = type;
    }

    @Override
    public T getResult(Stack stack) {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " " + name + " = " + value.toString();
    }
}
