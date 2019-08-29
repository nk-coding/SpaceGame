package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.*;

public class ModAssignmentIntegerOperation extends ModIntegerOperation implements Statement {

    private String name;

    public ModAssignmentIntegerOperation(String name){
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
