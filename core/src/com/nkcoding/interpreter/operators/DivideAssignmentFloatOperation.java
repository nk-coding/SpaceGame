package com.nkcoding.interpreter.operators;

import com.nkcoding.interpreter.ReturnException;
import com.nkcoding.interpreter.Stack;
import com.nkcoding.interpreter.StackItem;
import com.nkcoding.interpreter.Statement;

public class DivideAssignmentFloatOperation extends DivideFloatOperation implements Statement {

    private String name;

    public DivideAssignmentFloatOperation(String name) {
        this.name = name;
    }

    @Override
    public Float getResult(Stack stack) {
        Float res = super.getResult(stack);
        ((StackItem<Float>) stack.getFromStack(name)).setValue(res);
        return res;
    }

    @Override
    public void run(Stack stack) throws ReturnException {
        getResult(stack);
    }
}
