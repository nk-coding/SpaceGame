package com.nkcoding.interpreter;

public class RawValueExpression<T> implements Expression<T> {

    private final T value;

    private String type;

    public RawValueExpression(T value, String type){
        this.value = value;
        this.type = type;
    }

    @Override
    public T getResult(Stack stack){
        //System.out.println("Raw value expression: return " + value);
        return value;
    }

    @Override
    public String getType() {
        return type;
    }
}
