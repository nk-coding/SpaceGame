package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.*;

public class ModAssignmentDoubleOperation extends ModDoubleOperation implements Statement {

    private String name;

    public ModAssignmentDoubleOperation(String name){
        this.name = name;
    }

    @Override
    public Double getResult(Stack stack){
        Double res = super.getResult(stack);
        ((StackItem<Double>) stack.getFromStack(name)).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }

}
