package com.nkcoding.interpreter;

public class GetValueExpression<T> implements Expression<T> {

    private String name;

    public String getName() {
        return name;
    }

    private String type;

    public GetValueExpression(String name, String type){
        this.name = name;
        this.type = type;
    }

    @Override
    public T getResult(Stack stack) {
        //System.out.println("try get value: " + name);
        return ((StackItem<T>)stack.getFromStack(name)).getResult(stack);
    }

    @Override
    public String getType() {
        return type;
    }
}
