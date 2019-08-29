package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.*;

public class AddAssignmentStringOperation extends AddStringOperation implements Statement {

    private String name;

    public AddAssignmentStringOperation(String name){
        this.name = name;
    }

    @Override
    public String getResult(Stack stack){
        String res = super.getResult(stack);
        ((StackItem<String>) stack.getFromStack(name)).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }
}
