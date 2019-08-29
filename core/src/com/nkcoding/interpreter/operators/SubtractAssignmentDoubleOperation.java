package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.ReturnException;
import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.StackItem;
import com.nkcoding.interpreter.Statement;

public class SubtractAssignmentDoubleOperation extends SubtractDoubleOperation implements Statement {

    private String name;

    public SubtractAssignmentDoubleOperation(String name){
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
