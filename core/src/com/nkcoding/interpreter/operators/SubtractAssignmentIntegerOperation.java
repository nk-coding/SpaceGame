package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.ReturnException;
import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.StackItem;
import com.nkcoding.interpreter.Statement;

public class SubtractAssignmentIntegerOperation extends SubtractIntegerOperation implements Statement {

    private String name;

    public SubtractAssignmentIntegerOperation(String name){
        this.name = name;
    }

    @Override
    public Integer getResult(Stack stack){
        Integer res = super.getResult(stack);
        ((StackItem<Integer>) stack.getFromStack(name)).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }

}
